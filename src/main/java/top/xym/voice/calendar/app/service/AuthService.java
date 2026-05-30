package top.xym.voice.calendar.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.voice.calendar.app.model.dto.WxLoginDTO;
import top.xym.voice.calendar.app.model.entity.User;
import top.xym.voice.calendar.app.model.vo.UserLoginVO;

public interface AuthService extends IService<User> {

    /**
     * 微信登录
     *
     * @param loginDTO DTO
     * @return {@link UserLoginVO}
     */
    UserLoginVO weChatLogin(WxLoginDTO loginDTO);

    /**
     * 登出
     */
    void logout();

}
