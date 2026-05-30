package top.xym.voice.calendar.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"top.xym.voice.calendar.app.mapper"})
public class VoiceCalendarAppApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceCalendarAppApiApplication.class, args);
    }
}
