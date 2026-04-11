package devs.group5.rms.controllers;

import devs.group5.rms.dtos.LoginRequest;
import devs.group5.rms.dtos.SignUpRequest;
import devs.group5.rms.dtos.TokenResponse;
import devs.group5.rms.dtos.UserResponse;
import devs.group5.rms.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        val name = request.name();
        val token = authService.login(name);
        return new TokenResponse(token);
    }

    @PostMapping("/signup/admin")
    public UserResponse adminSignUp(@RequestBody SignUpRequest request) {
        val name = request.name();
        val admin = authService.signInAdmin(name);
        return new UserResponse(admin.getId(), admin.getName());
    }

    @PostMapping("/signup/owner")
    public UserResponse ownerSignUp(@RequestBody SignUpRequest request) {
        val name = request.name();
        val owner = authService.signInOwner(name);
        return new UserResponse(owner.getId(), owner.getName());
    }
}
