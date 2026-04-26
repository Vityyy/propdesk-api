package devs.group5.rms.repositories;

import devs.group5.rms.entities.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    List<Apartment> findByProperty_Owner_Id(UUID ownerId);

    List<Apartment> findByProperty_Id(UUID propertyId);
}
