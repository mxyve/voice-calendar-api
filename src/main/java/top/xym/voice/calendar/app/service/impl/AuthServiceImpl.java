package top.xym.voice.calendar.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.xym.voice.calendar.app.common.cache.RedisCache;
import top.xym.voice.calendar.app.common.cache.RedisKeys;
import top.xym.voice.calendar.app.common.cache.RequestContext;
import top.xym.voice.calendar.app.common.cache.TokenStoreCache;
import top.xym.voice.calendar.app.common.exception.ServerException;
import top.xym.voice.calendar.app.mapper.UserMapper;
import top.xym.voice.calendar.app.model.dto.WxLoginDTO;
import top.xym.voice.calendar.app.model.entity.User;
import top.xym.voice.calendar.app.model.vo.UserLoginVO;
import top.xym.voice.calendar.app.service.AuthService;
import top.xym.voice.calendar.app.utils.AESUtil;
import top.xym.voice.calendar.app.utils.JwtUtil;
import static top.xym.voice.calendar.app.common.constant.Constant.*;

import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {
    private final TokenStoreCache tokenStoreCache;
    private final RedisCache redisCache;

    @Override
    public UserLoginVO weChatLogin(WxLoginDTO loginDTO) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + APP_ID +
                "&secret=" + APP_SECRET +
                "&js_code=" + loginDTO.getCode() +
                "&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String jsonData = restTemplate.getForObject(url, String.class);
        if (StringUtils.contains(jsonData, WX_ERR_CODE)) {
            // 出错了
            throw new ServerException("openId获取失败," + jsonData);
        }
        // 解析返回数据
        JSONObject jsonObject = JSON.parseObject(jsonData);
        log.info("wxData: {}", jsonData);
        String openid = Objects.requireNonNull(jsonObject).getString(WX_OPENID
        );
        String sessionKey = jsonObject.getString(WX_SESSION_KEY);
        // 对⽤户加密数据解密
        String jsonUserData = AESUtil.decrypt(loginDTO.getEncryptedData(), sessionKey, loginDTO.getIv());
        log.info("wxUserInfo: {}", jsonUserData);
        JSONObject wxUserData = JSON.parseObject(jsonUserData);
        User user = baseMapper.getByWxOpenId(openid);
        if (ObjectUtils.isEmpty(user)) {
            log.info("⽤户不存在，创建⽤户, openId: {}", openid);
            user = new User();
            user.setWxOpenId(openid);
            user.setNickName(wxUserData.getString("nickName"));
            user.setAvatar(wxUserData.getString("avatarUrl"));
            baseMapper.insert(user);
        }

        String accessToken = JwtUtil.createToken(user.getUserId());
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getUserId());

        userLoginVO.setWxOpenId(user.getWxOpenId());
        userLoginVO.setAccessToken(accessToken);
        tokenStoreCache.saveUser(accessToken, userLoginVO);
        return userLoginVO;
    }

    @Override
    public void logout() {
        // 从上下⽂中获取userId，然后获取redisKey
        String cacheKey = RedisKeys.getUserIdKey(RequestContext.getUserId());
        // 通过userId，获取redis中的 accessToken
        String accessToken = (String) redisCache.get(cacheKey);
        // 删除缓存中的 token
        redisCache.delete(cacheKey);
        // 删除缓存中的⽤户信息
        tokenStoreCache.deleteUser(accessToken);
    }
}
