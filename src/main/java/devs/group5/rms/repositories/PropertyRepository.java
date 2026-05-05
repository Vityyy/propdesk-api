package devs.group5.rms.repositories;

import devs.group5.rms.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    List<Property> findByOwner_Id(UUID ownerId);
    List<Property> findByOwner_IdAndIsDeletedFalse(UUID ownerId);
}
