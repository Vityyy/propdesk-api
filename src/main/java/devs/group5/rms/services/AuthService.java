package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.OwnerRepository;
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
    private final AdminRepository adminRepository;
    private final OwnerRepository ownerRepository;

    @Transactional
    public String login(String name) {
        val user = userRepository.findByName(name).orElseThrow(() -> new BadCredentialsException("User does not exist"));
        return jwtService.generate(user.getId(), user.getRole());
    }

    @Transactional
    public Admin signInAdmin(String name) {
        var admin = Admin.builder().name(name).build();
        admin = adminRepository.save(admin);
        return admin;
    }

    @Transactional
    public Owner signInOwner(String name) {
        var owner = Owner.builder().name(name).build();
        owner = ownerRepository.save(owner);
        return owner;
    }
}
