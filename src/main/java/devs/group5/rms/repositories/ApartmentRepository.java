package devs.group5.rms.repositories;

import devs.group5.rms.entities.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    List<Apartment> findByProperty_Owner_Id(UUID ownerId);

    /**
     * Loads all apartments for a property in a single query, eagerly fetching
     * tenant and expenses to avoid N+1 / LazyInitializationException.
     */
    @Query("""
            SELECT DISTINCT a FROM Apartment a
            LEFT JOIN FETCH a.tenant
            LEFT JOIN FETCH a.expenses
            WHERE a.property.id = :propertyId
              AND a.isDeleted = false
              AND a.property.isDeleted = false
            """)
    List<Apartment> findByProperty_IdWithDetails(@Param("propertyId") UUID propertyId);

    @Query("""
            SELECT DISTINCT a FROM Apartment a
            JOIN a.property p
            LEFT JOIN FETCH a.tenant
            LEFT JOIN FETCH a.expenses
            WHERE p.owner.id = :ownerId
              AND a.isDeleted = false
              AND p.isDeleted = false
            """)
    List<Apartment> findByProperty_Owner_IdWithDetails(@Param("ownerId") UUID ownerId);

    List<Apartment> findByProperty_Id(UUID propertyId);

    List<Apartment> findByTenant_Id(UUID tenantId);

    List<Apartment> findByProperty_Owner_IdAndIsDeletedFalse(UUID ownerId);

    List<Apartment> findByProperty_IdAndIsDeletedFalse(UUID propertyId);

    List<Apartment> findByTenant_IdAndIsDeletedFalse(UUID tenantId);

    @Query("""
            SELECT a FROM Apartment a
            WHERE a.tenant.id = :tenantId
              AND a.isDeleted = false
              AND a.property.isDeleted = false
            """)
    List<Apartment> findActiveByTenantId(@Param("tenantId") UUID tenantId);
}
