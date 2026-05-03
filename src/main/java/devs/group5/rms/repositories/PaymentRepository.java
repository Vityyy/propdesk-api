package devs.group5.rms.repositories;

import devs.group5.rms.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    @Query("SELECT p FROM Payment p WHERE p.apartment.property.owner.id = :ownerId")
    List<Payment> findByOwnerId(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT p FROM Payment p WHERE p.apartment.property.owner.id = :ownerId AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    List<Payment> findByOwnerIdAndDateRange(
            @Param("ownerId") UUID ownerId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
