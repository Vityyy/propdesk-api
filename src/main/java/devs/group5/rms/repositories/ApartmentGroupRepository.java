package devs.group5.rms.repositories;

import devs.group5.rms.entities.ApartmentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentGroupRepository extends JpaRepository<ApartmentGroup, UUID> {
    List<ApartmentGroup> findByProperty_Id(UUID propertyId);
}

