package top.xym.voice.calendar.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.voice.calendar.app.common.result.Result;
import top.xym.voice.calendar.app.model.dto.WxLoginDTO;
import top.xym.voice.calendar.app.model.vo.UserLoginVO;
import top.xym.voice.calendar.app.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接⼝")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("weChatLogin")
    public Result<UserLoginVO> weChatLogin(@RequestBody WxLoginDTO dto) {
        return Result.ok(authService.weChatLogin(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Object> logout() {
        authService.logout();
        return Result.ok();
    }
}
