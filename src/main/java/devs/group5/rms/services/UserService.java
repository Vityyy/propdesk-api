package devs.group5.rms.services;

import devs.group5.rms.entities.User;
import devs.group5.rms.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByName(String name) {
        return userRepository
                .findByName(name)
                .orElseThrow(() -> new RuntimeException("Could not find user with name %s".formatted(name)));
    }
}
