package top.xym.voice.calendar.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.voice.calendar.app.model.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends IService<Schedule> {
    boolean addSchedule(Schedule schedule);
    List<Schedule> getByUserId(Integer userId);
    List<Schedule> getByDate(Integer userId, LocalDate date);
    List<Schedule> getByDateTime(Integer userId, LocalDate date, String time);
    boolean deleteById(Integer id);
    List<LocalDate> getMonthScheduleDates(Integer userId, int year, int month);
}
