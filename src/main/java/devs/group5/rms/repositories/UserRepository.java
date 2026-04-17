package devs.group5.rms.repositories;

import devs.group5.rms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByName(String name);

    boolean existsByName(String name);
}
