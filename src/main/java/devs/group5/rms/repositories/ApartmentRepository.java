package devs.group5.rms.repositories;

import devs.group5.rms.entities.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
}
