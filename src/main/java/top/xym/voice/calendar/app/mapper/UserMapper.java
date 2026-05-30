package top.xym.voice.calendar.app.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.xym.voice.calendar.app.model.entity.User;


public interface UserMapper extends BaseMapper<User> {

    default User getByWxOpenId(String openId) {
        return this.selectOne(new LambdaQueryWrapper<User>().eq(User::getWxOpenId, openId));
    }
}
