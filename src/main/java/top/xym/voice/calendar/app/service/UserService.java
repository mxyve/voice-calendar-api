package top.xym.voice.calendar.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.voice.calendar.app.model.entity.User;
import top.xym.voice.calendar.app.model.vo.UserInfoVO;

public interface UserService extends IService<User> {
    /**
     * ⽤户信息
     *
     * @return {@link UserInfoVO}
     */
    UserInfoVO userInfo();
}
