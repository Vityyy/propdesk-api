package devs.group5.rms.repositories;

import devs.group5.rms.entities.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    boolean existsByName(String name);

    boolean existsByIdAndAdmin_Id(UUID id, UUID adminId);

    Optional<Owner> findByIdAndAdmin_Id(UUID id, UUID adminId);
}
