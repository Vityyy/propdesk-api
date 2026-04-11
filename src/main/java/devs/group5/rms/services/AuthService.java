package devs.group5.rms.services;

import devs.group5.rms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public String login(String name) {
        val user = userRepository.findByName(name).orElseThrow(() -> new BadCredentialsException("User does not exist"));
        return jwtService.generate(user.getId());
    }
}
