package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.User;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User authenticate(String name, String password) {
        val user = userRepository
                .findByName(name)
                .orElseThrow(() -> new BadCredentialsException("Could not find user with name %s".formatted(name)));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password %s for user with id %s".formatted(name, password));
        }

        return user;
    }

    @Transactional
    public String refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid token type for refresh flow");
        }

        val userId = jwtService.extractUserId(refreshToken);

        val user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("Could not find user with id %s".formatted(userId)));

        return jwtService.generateAccessToken(user);
    }

    private void validateRegister(String name, String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }

        if (adminRepository.existsByName(name)) {
            throw new EntityExistsException();
        }
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional
    public Admin registerAdmin(String name, String password) {
        validateRegister(name, password);


        val hashedPassword = hashPassword(password);
        val admin = Admin
                .builder()
                .name(name)
                .password(hashedPassword)
                .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Owner registerOwner(String name, String password) {
        validateRegister(name, password);


        val hashedPassword = hashPassword(password);
        val owner = Owner
                .builder()
                .name(name)
                .password(hashedPassword)
                .build();

        return ownerRepository.save(owner);
    }
}
