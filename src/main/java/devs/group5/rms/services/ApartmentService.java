package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentExpenseData;
import devs.group5.rms.data.ApartmentWithTenantData;
import devs.group5.rms.data.TenantData;
import devs.group5.rms.dtos.ApartmentExpenseRequest;
import devs.group5.rms.dtos.TenantRequest;
import devs.group5.rms.entities.Expense;
import devs.group5.rms.entities.Tenant;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.ExpenseRepository;
import devs.group5.rms.repositories.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final TenantRepository tenantRepository;
    private final ExpenseRepository expenseRepository;
    private final devs.group5.rms.repositories.PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public Map<Integer, Map<Integer, ApartmentWithTenantData>> getApartmentsFromPropertyByFloor(UUID propertyId) {
        val apartments = apartmentRepository.findByProperty_Id(propertyId);

        val apartmentsWithTenants = apartments.stream()
                .map(apartment -> {
                    TenantData tenantData = null;
                    val tenant = apartment.getTenant();

                    if (tenant != null) {
                        tenantData = new TenantData(tenant.getId(), tenant.getName(), tenant.getPhone(), tenant.getEmail());
                    }

                    val expenseDataList = apartment.getExpenses().stream()
                            .map(e -> new ApartmentExpenseData(e.getId(), e.getAmount(), e.getDescription()))
                            .toList();

                    return new ApartmentWithTenantData(
                            apartment.getId(),
                            apartment.getNumber(),
                            apartment.getDueDate(),
                            apartment.getPaymentStatus(),
                            apartment.getFloor(),
                            apartment.getSquareMeters(),
                            apartment.getRent(),
                            tenantData,
                            expenseDataList
                    );
                })
                .toList();

        return apartmentsWithTenants
                .stream()
                .collect(Collectors.groupingBy(ApartmentWithTenantData::floor))
                .entrySet()
                .stream()
                .map(entry ->
                        Map.entry(
                                entry.getKey(),
                                entry.getValue()
                                        .stream()
                                        .collect(Collectors.groupingBy(ApartmentWithTenantData::number))
                                        .entrySet()
                                        .stream()
                                        .map(e -> Map.entry(e.getKey(), e.getValue().getFirst()))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public devs.group5.rms.entities.Apartment addSingleApartment(UUID ownerId, devs.group5.rms.dtos.SingleApartmentCreateRequest request) {
        val property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this property");
        }

        val apartment = devs.group5.rms.entities.Apartment.builder()
                .property(property)
                .floor(request.floor())
                .number(request.number())
                .rent(request.rent())
                .squareMeters(request.squareMeters())
                .paymentStatus(devs.group5.rms.entities.PaymentStatus.PENDING)
                .build();

        return apartmentRepository.save(apartment);
    }

    @Transactional
    public void deleteApartment(UUID ownerId, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        apartmentRepository.delete(apartment);
    }

    // ─── Tenant management ───────────────────────────────────────────────────

    /**
     * Assigns a tenant to an apartment. If a tenant with the same name already
     * exists in the DB, reuses it. Otherwise creates a new one.
     */
    @Transactional
    public TenantData assignOrCreateTenant(UUID ownerId, UUID apartmentId, TenantRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        // Reuse existing tenant by name or create a new one
        val tenant = tenantRepository.findByName(request.name())
                .orElseGet(() -> tenantRepository.save(
                        Tenant.builder()
                                .name(request.name())
                                .phone(request.phone())
                                .email(request.email())
                                .build()
                ));

        // Update phone/email in case they changed
        tenant.setPhone(request.phone());
        tenant.setEmail(request.email());
        tenantRepository.save(tenant);

        apartment.setTenant(tenant);
        apartmentRepository.save(apartment);

        return new TenantData(tenant.getId(), tenant.getName(), tenant.getPhone(), tenant.getEmail());
    }

    /**
     * Updates the tenant data of the tenant currently assigned to this apartment.
     */
    @Transactional
    public TenantData updateTenant(UUID ownerId, UUID apartmentId, TenantRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        val tenant = apartment.getTenant();
        if (tenant == null) {
            throw new IllegalArgumentException("This apartment has no tenant");
        }

        if (request.name() != null && !request.name().isBlank()) {
            tenant.setName(request.name());
        }
        tenant.setPhone(request.phone());
        tenant.setEmail(request.email());
        tenantRepository.save(tenant);

        return new TenantData(tenant.getId(), tenant.getName(), tenant.getPhone(), tenant.getEmail());
    }

    /**
     * Removes the tenant from this apartment.
     * If the tenant has no more apartments after this, deletes the tenant from the DB.
     */
    @Transactional
    public void vacateApartment(UUID ownerId, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        val tenant = apartment.getTenant();
        if (tenant == null) {
            return; // already vacant, nothing to do
        }

        apartment.setTenant(null);
        apartmentRepository.save(apartment);

        // If tenant has no more apartments, remove them from DB entirely
        val remainingApartments = apartmentRepository.findByTenant_Id(tenant.getId());
        if (remainingApartments.isEmpty()) {
            tenantRepository.delete(tenant);
        }
    }

    // ─── Expense management ──────────────────────────────────────────────────

    public List<ApartmentExpenseData> getExpensesForApartment(UUID ownerId, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        return expenseRepository.findByApartment_Id(apartmentId).stream()
                .map(e -> new ApartmentExpenseData(e.getId(), e.getAmount(), e.getDescription()))
                .toList();
    }

    @Transactional
    public ApartmentExpenseData addExpenseToApartment(UUID ownerId, UUID apartmentId, ApartmentExpenseRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        val expense = Expense.builder()
                .amount(request.amount())
                .description(request.description())
                .apartment(apartment)
                .build();

        val saved = expenseRepository.save(expense);
        return new ApartmentExpenseData(saved.getId(), saved.getAmount(), saved.getDescription());
    }

    @Transactional
    public void deleteExpenseFromApartment(UUID ownerId, UUID apartmentId, UUID expenseId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!apartment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("User is not the owner of this apartment");
        }

        val expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (!expense.getApartment().getId().equals(apartmentId)) {
            throw new IllegalArgumentException("Expense does not belong to this apartment");
        }

        expenseRepository.delete(expense);
    }
}
