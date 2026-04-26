package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentWithTenantData;
import devs.group5.rms.data.TenantData;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final TenantRepository tenantRepository;

    public Map<Integer, Map<Integer, ApartmentWithTenantData>> getApartmentsFromPropertyByFloor(UUID propertyId) {
        val apartments = apartmentRepository.findByProperty_Id(propertyId);


        val apartmentsWithTenants = apartments.stream()
                .map(apartment -> {
                    TenantData tenantData = null;
                    val tenant = apartment.getTenant();

                    if (tenant != null) {
                        tenantData = new TenantData(tenant.getId(), tenant.getName());
                    }

                    return new ApartmentWithTenantData(
                            apartment.getId(),
                            apartment.getNumber(),
                            apartment.getDueDate(),
                            apartment.getPaymentStatus(),
                            apartment.getFloor(),
                            apartment.getSquareMeters(),
                            apartment.getRent(),
                            tenantData
                    );
                })
                .toList();

        return apartmentsWithTenants
                .stream()
                .collect(Collectors.groupingBy(ApartmentWithTenantData::floor)) // Group by floor
                .entrySet()
                .stream()
                .map(entry ->
                        Map.entry(
                                entry.getKey(),
                                entry.getValue()
                                        .stream()
                                        .collect(Collectors.groupingBy(ApartmentWithTenantData::number)) // Group by apartment number
                                        .entrySet()
                                        .stream()
                                        .map(e -> Map.entry(e.getKey(), e.getValue().getFirst()))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @org.springframework.transaction.annotation.Transactional
    public devs.group5.rms.entities.Apartment updateApartment(UUID ownerId, UUID apartmentId, devs.group5.rms.dtos.ApartmentUpdateRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        if (request.rent() != null) {
            apartment.setRent(request.rent());
        }
        if (request.squareMeters() != null) {
            apartment.setSquareMeters(request.squareMeters());
        }

        return apartmentRepository.save(apartment);
    }

    @org.springframework.transaction.annotation.Transactional
    public void bulkUpdateApartments(UUID ownerId, devs.group5.rms.dtos.ApartmentBulkUpdateRequest request) {
        if (request.apartmentIds() == null || request.apartmentIds().isEmpty()) {
            return;
        }

        val apartments = apartmentRepository.findAllById(request.apartmentIds());
        for (val apartment : apartments) {
            if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
                throw new IllegalArgumentException("User is not the owner of all specified apartments");
            }

            if (request.rent() != null) {
                apartment.setRent(request.rent());
            }
            if (request.squareMeters() != null) {
                apartment.setSquareMeters(request.squareMeters());
            }
        }

        apartmentRepository.saveAll(apartments);
    }
}
