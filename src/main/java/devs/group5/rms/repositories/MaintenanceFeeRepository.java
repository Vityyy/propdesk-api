package devs.group5.rms.repositories;

import devs.group5.rms.entities.MaintenanceFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceFeeRepository extends JpaRepository<MaintenanceFee, UUID> {
    List<MaintenanceFee> findByApartment_Id(UUID apartmentId);
}
