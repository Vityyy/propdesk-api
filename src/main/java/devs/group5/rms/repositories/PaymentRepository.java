package devs.group5.rms.repositories;

import devs.group5.rms.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    @Query("SELECT p FROM Payment p WHERE p.apartment.property.owner.id = :ownerId AND p.isCancelled = false AND p.apartment.isDeleted = false AND p.apartment.property.isDeleted = false")
    List<Payment> findByOwnerId(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT p FROM Payment p WHERE p.apartment.property.owner.id = :ownerId AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate AND p.isCancelled = false AND p.apartment.isDeleted = false AND p.apartment.property.isDeleted = false")
    List<Payment> findByOwnerIdAndDateRange(
            @Param("ownerId") UUID ownerId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    java.util.Optional<Payment> findByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
            UUID apartmentId, 
            devs.group5.rms.entities.PaymentType type, 
            Integer billingYear, 
            Integer billingMonth);

    List<Payment> findAllByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
            UUID apartmentId, 
            devs.group5.rms.entities.PaymentType type, 
            Integer billingYear, 
            Integer billingMonth);
}
