package top.xym.voice.calendar.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.xym.voice.calendar.app.common.cache.RequestContext;
import top.xym.voice.calendar.app.common.result.Result;
import top.xym.voice.calendar.app.model.entity.Schedule;
import top.xym.voice.calendar.app.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedule")
@AllArgsConstructor
@Tag(name = "日程接口")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping("/add")
    @Operation(summary = "添加日程")
    public Result<Boolean> add(@RequestBody Schedule schedule) {
        return Result.ok(scheduleService.addSchedule(schedule));
    }

    @GetMapping("/list")
    @Operation(summary = "我的所有日程")
    public Result<List<Schedule>> list() {
        Integer userId = RequestContext.getUserId();
        System.out.println("用户userId：" + userId);
        return Result.ok(scheduleService.getByUserId(userId));
    }

    @GetMapping("/date")
    @Operation(summary = "按日期查询（支持日期或日期+时间）")
    public Result<List<Schedule>> date(
            @RequestParam(required = true) String date,
            @RequestParam(required = false) String time) {
        Integer userId = RequestContext.getUserId();
        LocalDate queryDate = LocalDate.parse(date);

        if (StringUtils.isNotBlank(time)) {
            // 如果时间没有秒，自动补上 :00
            String timeWithSeconds = time;
            if (time.split(":").length == 2) {
                timeWithSeconds = time + ":00";
            }
            System.out.println("查询时间：" + time + " -> 存储格式：" + timeWithSeconds);
            return Result.ok(scheduleService.getByDateTime(userId, queryDate, timeWithSeconds));
        } else {
            return Result.ok(scheduleService.getByDate(userId, queryDate));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.ok(scheduleService.deleteById(id));
    }

    @GetMapping("/month")
    @Operation(summary = "按月份查询有日程的日期（返回该月有日程的日期列表）")
    public Result<List<LocalDate>> month(
            @RequestParam int year,
            @RequestParam int month) {
        Integer userId = RequestContext.getUserId();
        System.out.println("按月份查询，用户ID：" + userId + "，年份：" + year + "，月份：" + month);
        return Result.ok(scheduleService.getMonthScheduleDates(userId, year, month));
    }
}