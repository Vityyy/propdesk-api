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
    public User authenticate(String email, String password) {
        val user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
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

    private void validateRegister(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }

        if (userRepository.existsByEmail(email)) {
            throw new EntityExistsException("Email already in use");
        }
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional
    public Admin registerAdmin(String name, String email, String password) {
        validateRegister(name, email, password);


        val hashedPassword = hashPassword(password);
        val admin = Admin
                .builder()
                .name(name)
                .email(email)
                .password(hashedPassword)
                .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Owner registerOwner(String name, String email, String password) {
        validateRegister(name, email, password);


        val hashedPassword = hashPassword(password);
        val owner = Owner
                .builder()
                .name(name)
                .email(email)
                .password(hashedPassword)
                .build();

        return ownerRepository.save(owner);
    }
}
