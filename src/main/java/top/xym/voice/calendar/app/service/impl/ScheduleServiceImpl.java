package top.xym.voice.calendar.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import top.xym.voice.calendar.app.common.cache.RequestContext;
import top.xym.voice.calendar.app.common.exception.ServerException;
import top.xym.voice.calendar.app.mapper.ScheduleMapper;
import top.xym.voice.calendar.app.model.entity.Schedule;
import top.xym.voice.calendar.app.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Override
    public boolean addSchedule(Schedule schedule) {
        Integer userId = RequestContext.getUserId();
        schedule.setUserId(userId);
        schedule.setDeleted(0);

        // 验证日期不能为空
        if (schedule.getScheduleDate() == null) {
            throw new ServerException("日程日期不能为空");
        }

        System.out.println("添加日程，用户ID：" + userId + "，日期：" + schedule.getScheduleDate() + "，时间：" + schedule.getScheduleTime());
        return save(schedule);
    }

    @Override
    public List<Schedule> getByUserId(Integer userId) {
        return lambdaQuery()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getDeleted, 0)
                .orderByDesc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getScheduleTime)
                .list();
    }

    @Override
    public List<Schedule> getByDate(Integer userId, LocalDate date) {
        System.out.println("按日期查询，用户ID：" + userId + "，日期：" + date);

        return lambdaQuery()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getDeleted, 0)
                .eq(Schedule::getScheduleDate, date)
                .orderByAsc(Schedule::getScheduleTime)
                .list();
    }

    @Override
    public List<Schedule> getByDateTime(Integer userId, LocalDate date, String time) {
        System.out.println("按日期+时间查询，用户ID：" + userId + "，日期：" + date + "，时间：" + time);

        return lambdaQuery()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getDeleted, 0)
                .eq(Schedule::getScheduleDate, date)
                .and(wrapper -> wrapper
                        .eq(Schedule::getScheduleTime, time)
                        .or()
                        .eq(Schedule::getScheduleTime, time + ":00")  // 支持不带秒的查询
                        .or()
                        .isNull(Schedule::getScheduleTime)
                        .or()
                        .eq(Schedule::getScheduleTime, "")
                )
                .orderByAsc(Schedule::getScheduleTime)
                .list();
    }

    @Override
    public boolean deleteById(Integer id) {
        Integer userId = RequestContext.getUserId();
        Schedule schedule = getById(id);
        if (schedule == null || !schedule.getUserId().equals(userId)) {
            throw new ServerException("无权限");
        }
        System.out.println("删除日程，ID：" + id + "，用户ID：" + userId);
        return removeById(id);
    }

    @Override
    public List<LocalDate> getMonthScheduleDates(Integer userId, int year, int month) {
        // 查询指定月份有日程的日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        List<Schedule> schedules = lambdaQuery()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getDeleted, 0)
                .ge(Schedule::getScheduleDate, startDate)
                .lt(Schedule::getScheduleDate, endDate)
                .select(Schedule::getScheduleDate)
                .list();

        // 提取日期并去重
        return schedules.stream()
                .map(Schedule::getScheduleDate)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
}