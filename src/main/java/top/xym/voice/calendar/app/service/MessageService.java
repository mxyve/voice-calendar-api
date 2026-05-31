package top.xym.voice.calendar.app.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.xym.voice.calendar.app.common.cache.RequestContext;
import top.xym.voice.calendar.app.model.dto.MessageSendRequest;
import top.xym.voice.calendar.app.tool.CalendarTool;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatModel chatModel;
    private final CalendarTool calendarTool;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.nls.app-key}")
    private String NLS_APP_KEY;

    private ChatClient chatClient;
    private String nlsToken;
    private long tokenExpireTime;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultTools(calendarTool)
                .build();
    }

    /**
     * 语音日历AI对话（一次性返回，不存库，不存会话）
     */
    public Map<String, String> sendMessage(MessageSendRequest request) {
        Integer userId = RequestContext.getUserId();
        System.out.println("AI对话，用户ID：" + userId);

        // 1. 语音识别
        if (StringUtils.isNotBlank(request.getAudio())) {
            try {
                String text = qwenAsrByHttp(request.getAudio());
                request.setContent(text);
            } catch (Exception e) {
                throw new RuntimeException("语音识别失败");
            }
        }

        if (StringUtils.isBlank(request.getContent())) {
            throw new RuntimeException("消息不能为空");
        }

        // 2. 构建AI提示词
        Prompt prompt = buildPrompt(request.getContent());

        // 3. 调用AI + 工具
        String aiResult = chatClient.prompt(prompt).call().content();
        System.out.println("AI返回结果：{}" + aiResult);

        // 4. 语音合成
        String audioBase64 = "";
        try {
            byte[] audioBytes = textToSpeech(aiResult);
            audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
            System.out.println("语音合成成功，音频大小：{} bytes" + audioBytes.length);
        } catch (Exception ignored) {
        }

        // 5. 一次性返回
        return Map.of(
                "text", aiResult,
                "audio", audioBase64
        );
    }

    /**
     * 构建提示词（语音日历专用）
     */
    private Prompt buildPrompt(String userContent) {
        List<Message> messages = new ArrayList<>();

        // 获取当前真实日期
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();
        String yesterdayStr = today.minusDays(1).toString();
        String tomorrowStr = today.plusDays(1).toString();
        System.out.println("今天" + todayStr + "昨天" + yesterdayStr + "明天" + tomorrowStr);

        String systemPrompt = """
                你是智能语音日历助手，只做日程管理。
                今天是 %s，昨天是 %s，明天是 %s。
                            
                规则：
                1. 用户要【添加、创建、提醒】 → 调用 addSchedule，参数：title（标题）、scheduleDate（日期 yyyy-MM-dd）、scheduleTime（时间 HH:mm，可选）、content（备注，可选）
                2. 用户要【查看、查询、今天、昨天、明天、某日日程】→ 调用 listSchedule，参数：date（日期 yyyy-MM-dd）
                3. 用户要【删除、取消日程】→ 调用 deleteSchedule，参数：id（日程ID）
                4. 回答必须简洁、口语化、自然
                5. 不要返回JSON，不要返回格式
                6. 如果用户说今天，日期就是 %s；说昨天，日期就是 %s；说明天，日期就是 %s
                7. 时间格式：如果是下午2点，转换成14:00
                """.formatted(todayStr, yesterdayStr, tomorrowStr, todayStr, yesterdayStr, tomorrowStr);

        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userContent));
        return new Prompt(messages);
    }

    // ==================== 阿里云语音识别 ASR ====================
    private String qwenAsrByHttp(String base64Audio) {
        try {
            String pureBase64 = base64Audio.contains(",") ? base64Audio.split(",")[1] : base64Audio;
            byte[] audioBytes = Base64.getDecoder().decode(pureBase64);
            String token = getNlsToken();

            String asrUrl = "https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/asr"
                    + "?appkey=" + NLS_APP_KEY
                    + "&format=mp3"
                    + "&sample_rate=16000";

            HttpURLConnection conn = (HttpURLConnection) new URL(asrUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            conn.setRequestProperty("Host", "nls-gateway-cn-shanghai.aliyuncs.com");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Length", String.valueOf(audioBytes.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(audioBytes);
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream in = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            String result = IoUtil.read(in, StandardCharsets.UTF_8);

            JSONObject json = JSON.parseObject(result);
            if ("20000000".equals(json.getString("status"))) {
                return json.getString("result");
            } else {
                throw new RuntimeException("识别失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("语音识别异常");
        }
    }

    // ==================== 语音合成 TTS ====================
    public byte[] textToSpeech(String text) {
        try {
            String token = getNlsToken();
            URL url = new URL("https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "audio/wav;codec=pcm;rate=16000");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setDoOutput(true);

            JSONObject params = new JSONObject();
            params.put("appkey", NLS_APP_KEY);
            params.put("text", text);
            params.put("voice", "xiaoyun");
            params.put("format", "mp3");
            params.put("sample_rate", 16000);
            params.put("volume", 50);
            params.put("speed", 0);
            params.put("pitch", 0);

            conn.getOutputStream().write(params.toString().getBytes(StandardCharsets.UTF_8));

            if (conn.getResponseCode() != 200) {
                return new byte[0];
            }

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // ==================== 获取阿里云语音Token ====================
    public String getNlsToken() {
        if (nlsToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return nlsToken;
        }
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setDomain("nls-meta.cn-shanghai.aliyuncs.com");
            request.setVersion("2019-02-28");
            request.setAction("CreateToken");
            request.setMethod(MethodType.GET);
            request.setProtocol(ProtocolType.HTTPS);

            CommonResponse response = client.getCommonResponse(request);
            JSONObject json = JSON.parseObject(response.getData());

            nlsToken = json.getJSONObject("Token").getString("Id");
            long expire = json.getJSONObject("Token").getLong("ExpireTime");
            tokenExpireTime = expire * 1000 - 120000;

            return nlsToken;
        } catch (Exception e) {
            throw new RuntimeException("获取阿里云Token失败");
        }
    }
}