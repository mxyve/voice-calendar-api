package top.xym.voice.calendar.app.common.constant;

public interface Constant {
    //创建时间
    String CREATE_TIME = "createTime";
    //更新时间
    String UPDATE_TIME = "updateTime";
    //逻辑删除
    String DELETED = "deleted";
    // ⽤户id
    String USER_ID = "userId";

    // 微信小程序 appId
    String APP_ID = "wx6f68bfa2a7373025";
    // appSecret
    String APP_SECRET = "a0e99ba45494a7c7ac1bcdb16a244810";
    // appSecret
    // 微信返回参数中的属性名
    String WX_ERR_CODE = "errcode";
    // 返回参数中的属性名
    String WX_OPENID = "openid";
    // 返回参数中的属性名
    String WX_SESSION_KEY = "session_key";
    // 前端没有登录的时候会携带的token，后续会⽤到
    String NO_TOKEN = "no-token";
}