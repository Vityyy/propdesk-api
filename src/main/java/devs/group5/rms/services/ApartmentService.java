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
                .map(apartment -> new ApartmentWithTenantData(
                        apartment.getId(),
                        apartment.getNumber(),
                        apartment.getDueDate(),
                        apartment.getPaymentStatus(),
                        apartment.getFloor(),
                        apartment.getSquareMeters(),
                        apartment.getRent(),
                        new TenantData(
                                apartment.getTenant().getId(),
                                apartment.getTenant().getName()
                        )
                ))
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
}
