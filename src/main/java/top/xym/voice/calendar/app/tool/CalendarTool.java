package top.xym.voice.calendar.app.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import top.xym.voice.calendar.app.common.cache.RequestContext;
import top.xym.voice.calendar.app.model.entity.Schedule;
import top.xym.voice.calendar.app.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

@Component
public class CalendarTool {
    private final ScheduleService scheduleService;

    public CalendarTool(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Tool(description = "添加日程，title：标题，scheduleDate：日期（格式yyyy-MM-dd），scheduleTime：时间（可选，格式HH:mm），content：备注")
    public String addSchedule(String title, String scheduleDate, String scheduleTime, String content) {
        Integer userId = RequestContext.getUserId();
        Schedule s = new Schedule();
        s.setUserId(userId);
        s.setTitle(title);
        s.setContent(content);
        s.setScheduleDate(LocalDate.parse(scheduleDate));
        s.setDeleted(0);
        if (scheduleTime != null && !scheduleTime.isEmpty()) {
            s.setScheduleTime(scheduleTime + ":00");  // 自动补上秒
        }
        scheduleService.save(s);
        return "添加成功：" + title;
    }

    @Tool(description = "查询某天的日程，date：日期 格式yyyy-MM-dd")
    public String listSchedule(String date) {
        Integer userId = RequestContext.getUserId();
        LocalDate d = LocalDate.parse(date);
        List<Schedule> list = scheduleService.getByDate(userId, d);
        if (list.isEmpty()) {
            return "当天无日程";
        }
        StringBuilder sb = new StringBuilder("当天日程：\n");
        for (Schedule s : list) {
            if (s.getScheduleTime() != null && !s.getScheduleTime().isEmpty()) {
                // 显示时去掉秒
                String time = s.getScheduleTime().substring(0, 5);
                sb.append(time).append(" ").append(s.getTitle()).append("\n");
            } else {
                sb.append("全天 ").append(s.getTitle()).append("\n");
            }
        }
        return sb.toString();
    }

    @Tool(description = "删除日程，id：日程ID")
    public String deleteSchedule(Integer id) {
        boolean b = scheduleService.deleteById(id);
        return b ? "删除成功" : "删除失败";
    }
}