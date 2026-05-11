package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentRangeData;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.Property;
import devs.group5.rms.entities.Role;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import devs.group5.rms.repositories.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final AdminRepository adminRepository;
    private final devs.group5.rms.repositories.ApartmentRepository apartmentRepository;
    private final TenantRepository tenantRepository;

    private void validateApartmentsData(List<ApartmentRangeData> apartmentsData) {
        for (int i = 0; i < apartmentsData.size(); i++) {
            for (int j = i + 1; j < apartmentsData.size(); j++) {
                val overlaps = apartmentsData.get(i).overlapsWith(apartmentsData.get(j));

                if (overlaps) {
                    throw new IllegalArgumentException("Cannot add property with overlapping apartment ranges");
                }
            }
        }
    }

    private Stream<Apartment> createApartmentsFromRange(ApartmentRangeData apartmentsRange, Property property) {
        return apartmentsRange
                .floorsAsRange()
                .mapToObj(floor -> apartmentsRange
                        .apartmentNumbersAsRange()
                        .mapToObj(apartmentNumber -> Apartment
                                .builder()
                                .number(apartmentNumber)
                                .squareMeters(apartmentsRange.squareMeters())
                                .floor(floor)
                                .rent(apartmentsRange.rentValue())
                                .paymentStatus(devs.group5.rms.entities.PaymentStatus.PENDING)
                                .property(property)
                                .build()
                        )
                )
                .flatMap(apartments -> apartments);
    }

    private List<Apartment> createApartmentsFromRanges(List<ApartmentRangeData> apartmentRanges, Property property) {
        return apartmentRanges
                .stream()
                .flatMap(apartments -> createApartmentsFromRange(apartments, property))
                .toList();
    }

    private void ensureCanManageOwner(UUID authenticatedUserId, Role authenticatedUserRole, UUID ownerId) {
        if (authenticatedUserRole == Role.OWNER) {
            if (!authenticatedUserId.equals(ownerId)) {
                throw new IllegalArgumentException("Owners cannot access other owners resources");
            }
            return;
        }

        if (!ownerRepository.existsByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(ownerId, authenticatedUserId)) {
            throw new IllegalArgumentException("Admin does not manage this owner");
        }
    }

    @Transactional
    public Property addProperty(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            String propertyName,
            String propertyAddress,
            URL imageUrl,
            UUID ownerId,
            List<ApartmentRangeData> apartmentRanges
    ) {
        val owner = switch (authenticatedUserRole) {
            case ADMIN -> ownerRepository
                    .findByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(ownerId, authenticatedUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin %s does not work with owner %s".formatted(authenticatedUserId, ownerId)));

            case OWNER -> {
                if (!authenticatedUserId.equals(ownerId)) {
                    throw new IllegalArgumentException("Owners cannot add properties to other owners");
                }

                yield ownerRepository
                        .findById(ownerId)
                        .orElseThrow(() -> new IllegalArgumentException("Could not find owner with id %s".formatted(ownerId)));
            }
        };

        validateApartmentsData(apartmentRanges);

        val existingProperty = propertyRepository.findByOwner_IdAndNameAndAddress(owner.getId(), propertyName, propertyAddress);
        if (existingProperty.isPresent()) {
            val property = existingProperty.get();

            if (!property.isDeleted()) {
                throw new IllegalArgumentException("Property with same name and address already exists for this owner");
            }

            property.setDeleted(false);
            property.setImageUrl(imageUrl);
            property.setName(propertyName);
            property.setAddress(propertyAddress);
            val apartments = createApartmentsFromRanges(apartmentRanges, property);
            property.getApartments().addAll(apartments);

            return propertyRepository.save(property);
        }

        val property = Property
                .builder()
                .name(propertyName)
                .address(propertyAddress)
                .imageUrl(imageUrl)
                .owner(owner)
                .build();

        val apartments = createApartmentsFromRanges(apartmentRanges, property);

        property.getApartments().addAll(apartments);

        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(UUID authenticatedUserId, Role authenticatedUserRole, UUID propertyId, String newName, String newAddress, URL newImageUrl) {
        val property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, property.getOwner().getId());

        if (newName != null && !newName.trim().isEmpty()) {
            property.setName(newName);
        }
        if (newAddress != null && !newAddress.trim().isEmpty()) {
            property.setAddress(newAddress);
        }
        if (newImageUrl != null) {
            property.setImageUrl(newImageUrl);
        }

        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(UUID authenticatedUserId, Role authenticatedUserRole, UUID propertyId) {
        val property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, property.getOwner().getId());
        if (property.isDeleted()) {
            return;
        }

        val tenantsToCheck = apartmentRepository.findByProperty_Id(propertyId)
                .stream()
                .map(Apartment::getTenant)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        property.setDeleted(true);
        property.setName("%s [deleted:%s]".formatted(property.getName(), property.getId()));
        val apartments = apartmentRepository.findByProperty_Id(propertyId);
        for (val apartment : apartments) {
            apartment.setDeleted(true);
            apartment.setTenant(null);
            apartment.setDueDate(null);
        }
        apartmentRepository.saveAll(apartments);
        propertyRepository.save(property);

        for (val tenant : tenantsToCheck) {
            val activeApartments = apartmentRepository.findActiveByTenantId(tenant.getId());
            if (activeApartments.isEmpty()) {
                tenantRepository.delete(tenant);
            }
        }
    }
}
