package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.data.PropertyData;
import devs.group5.rms.dtos.PropertyApartmentsResponse;
import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final AdminRepository adminRepository;
    private final PropertyRepository propertyRepository;
    private final ApartmentRepository apartmentRepository;

    @PreAuthorize("hasRole('OWNER')")
    public List<Property> getProperties(@NonNull UUID ownerId) {
        return propertyRepository.findByOwner_Id(ownerId);
    }

    @PreAuthorize("hasRole('OWNER')")
    public List<Apartment> getApartments(@NonNull UUID ownerId) {
        return apartmentRepository.findByProperty_Owner_Id(ownerId);
    }

    @PreAuthorize("hasRole('OWNER')")
    public Property addProperty(
            @NonNull UUID authenticatedUserId,
            @NonNull PropertyData data
    ) {
        var owner = ownerRepository.findById(data.ownerId()).orElseThrow(() -> new RuntimeException(""));

        if (!owner.getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Owner does not match authenticated user");
        }

        var property = Property.builder()
                .name(data.name())
                .address(data.address())
                .owner(owner)
                .build();

        propertyRepository.save(property);
        return property;
    }

    @PreAuthorize("hasRole('OWNER')")
    public void deleteProperty(@NonNull UUID authenticatedUserId, @NonNull UUID propertyId) {
        var property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Property does not belong to authenticated user");
        }

        propertyRepository.delete(property);
    }

    @PreAuthorize("hasRole('OWNER')")
    public Apartment addApartment(UUID authenticatedUserId, @NonNull ApartmentData data) {
        var property = propertyRepository.findById(data.propertyId()).orElseThrow(() -> new RuntimeException(""));

        if (!property.getOwner().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Property does not belong to authenticated user");
        }

        var apartment = Apartment.builder()
                .number(data.number())
                .property(property)
                .rent(data.rent())
                .build();

        apartmentRepository.save(apartment);
        return apartment;
    }

    @PreAuthorize("hasRole('OWNER')")
    public List<PropertyApartmentsResponse> getPropertyApartmentsGrid(
            @NonNull UUID authenticatedUserId,
            @NonNull UUID propertyId
    ) {
        var property = propertyRepository.findById(propertyId).orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Property does not belong to authenticated user");
        }

        return apartmentRepository.findByProperty_Id(propertyId)
                .stream()
                .map(apartment -> new PropertyApartmentsResponse(
                        apartment.getId(),
                        apartment.getAmountDue(),
                        apartment.getPaymentStatus(),
                        apartment.getTenant() != null ? apartment.getTenant().getId() : null,
                        apartment.getTenant() != null ? apartment.getTenant().getName() : null,
                        null, // mts2 etc with the ApartmentGroup table. flatmap bersi blahblah
                        apartment.getDueDate()
                ))
                .toList();
    }

    // Associates the authenticated owner with an existing admin account.
    @PreAuthorize("hasRole('OWNER')")
    @Transactional
    public Owner associateAdmin(
            @NonNull UUID authenticatedUserId,
            @NonNull UUID adminId,
            BigDecimal adminCut
    ) {
        Owner owner = ownerRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Owner does not exist"));

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        if (adminCut != null && adminCut.signum() <= 0) {
            throw new IllegalArgumentException("Admin cut must be positive");
        }

        owner.setAdmin(admin);
        owner.setAdminCut(adminCut);

        return ownerRepository.save(owner);
    }
}
