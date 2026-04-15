package devs.group5.rms.controllers;

import devs.group5.rms.dtos.LoginRequest;
import devs.group5.rms.dtos.SignUpRequest;
import devs.group5.rms.dtos.TokenResponse;
import devs.group5.rms.dtos.UserResponse;
import devs.group5.rms.services.AuthService;
import devs.group5.rms.services.CookieService;
import devs.group5.rms.services.JwtService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        val user = authService.authenticate(request.name(), request.password());

        val accessToken = jwtService.generateAccessToken(user);
        val refreshToken = jwtService.generateRefreshToken(user);

        val cookie = cookieService.createRefreshTokenCookie(refreshToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponse(accessToken));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        val accessToken = authService.refresh(refreshToken);
        return new TokenResponse(accessToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        val cookie = cookieService.deleteRefreshTokenCookie();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/register/admin")
    public UserResponse registerAdmin(@RequestBody SignUpRequest request) {
        val admin = authService.registerAdmin(
                request.name(),
                request.password()
        );
        return new UserResponse(admin.getId(), admin.getName());
    }

    @PostMapping("/register/owner")
    public UserResponse registerOwner(@RequestBody SignUpRequest request) {
        val owner = authService.registerOwner(
                request.name(),
                request.password()
        );
        return new UserResponse(owner.getId(), owner.getName());
    }
}
