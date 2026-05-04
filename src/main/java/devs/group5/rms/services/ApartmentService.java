package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentExpenseData;
import devs.group5.rms.data.ApartmentWithTenantData;
import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.data.MaintenanceFeeData;
import devs.group5.rms.data.TenantData;
import devs.group5.rms.dtos.ApartmentExpenseRequest;
import devs.group5.rms.dtos.MaintenanceFeeRequest;
import devs.group5.rms.dtos.TenantRequest;
import devs.group5.rms.dtos.TenantRequest;
import devs.group5.rms.entities.Expense;
import devs.group5.rms.entities.MaintenanceFee;
import devs.group5.rms.entities.Role;
import devs.group5.rms.entities.Tenant;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.ExpenseRepository;
import devs.group5.rms.repositories.MaintenanceFeeRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
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
    private final MaintenanceFeeRepository maintenanceFeeRepository;
    private final devs.group5.rms.repositories.PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final devs.group5.rms.repositories.PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Map<Integer, Map<Integer, ApartmentWithTenantData>> getApartmentsFromPropertyByFloor(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            UUID propertyId
    ) {
        val property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, property.getOwner().getId());

        val apartments = apartmentRepository.findByProperty_IdWithDetails(propertyId);

        val apartmentsWithTenants = apartments.stream()
                .map(this::mapToApartmentWithTenantData)
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

    @Transactional(readOnly = true)
    public Map<UUID, Map<Integer, Map<Integer, ApartmentWithTenantData>>> getApartmentsFromOwnerGroupedByProperty(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            UUID ownerId
    ) {
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, ownerId);
        val apartments = apartmentRepository.findByProperty_Owner_IdWithDetails(ownerId);

        return apartments.stream()
                .collect(Collectors.groupingBy(
                        apartment -> apartment.getProperty().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::mapToApartmentWithTenantData, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .collect(Collectors.groupingBy(
                                        ApartmentWithTenantData::floor,
                                        LinkedHashMap::new,
                                        Collectors.toMap(
                                                ApartmentWithTenantData::number,
                                                apartmentData -> apartmentData,
                                                (first, second) -> first,
                                                LinkedHashMap::new
                                        )
                                )),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    @Transactional(readOnly = true)
    public List<devs.group5.rms.entities.Apartment> getApartmentsForUser(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            UUID ownerId
    ) {
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, ownerId);
        return apartmentRepository.findByProperty_Owner_IdAndIsDeletedFalse(ownerId);
    }

    @Transactional
    public devs.group5.rms.entities.Apartment addApartment(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            ApartmentData data
    ) {
        val property = propertyRepository.findById(data.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, property.getOwner().getId());

        val apartment = devs.group5.rms.entities.Apartment.builder()
                .number(data.number())
                .floor(1)
                .property(property)
                .rent(data.rent())
                .squareMeters(java.math.BigDecimal.ONE)
                .paymentStatus(devs.group5.rms.entities.PaymentStatus.PAID)
                .build();

        return apartmentRepository.save(apartment);
    }

    private void ensureCanManageOwner(UUID authenticatedUserId, Role authenticatedUserRole, UUID ownerId) {
        if (authenticatedUserRole == Role.OWNER) {
            if (!authenticatedUserId.equals(ownerId)) {
                throw new IllegalArgumentException("Owner cannot access another owner resources");
            }
            return;
        }

        // ADMIN
        if (!ownerRepository.existsByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(ownerId, authenticatedUserId)) {
            throw new IllegalArgumentException("Admin does not manage this owner");
        }
    }

    private ApartmentWithTenantData mapToApartmentWithTenantData(devs.group5.rms.entities.Apartment apartment) {
        TenantData tenantData = null;
        val tenant = apartment.getTenant();

        if (tenant != null) {
            tenantData = new TenantData(tenant.getId(), tenant.getName(), tenant.getPhone(), tenant.getEmail());
        }

        val expenseDataList = apartment.getExpenses().stream()
                .map(e -> new ApartmentExpenseData(e.getId(), e.getAmount(), e.getDescription()))
                .toList();

        // Fetch maintenance fees explicitly to avoid lazy-load issues
        val maintenanceFeeDataList = maintenanceFeeRepository.findByApartment_Id(apartment.getId()).stream()
                .map(f -> new MaintenanceFeeData(f.getId(), f.getCategory(), f.getDescription(), f.getAmount()))
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
                expenseDataList,
                maintenanceFeeDataList
        );
    }

    @Transactional
    public devs.group5.rms.entities.Apartment updateApartment(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            UUID apartmentId,
            devs.group5.rms.dtos.ApartmentUpdateRequest request
    ) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        if (request.rent() != null) {
            apartment.setRent(request.rent());
        }
        if (request.squareMeters() != null) {
            apartment.setSquareMeters(request.squareMeters());
        }
        if (request.dueDate() != null) {
            apartment.setDueDate(request.dueDate());
        }
        if (request.paymentStatus() != null) {
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            int currentYear = java.time.LocalDate.now().getYear();

            if (request.paymentStatus() == devs.group5.rms.entities.PaymentStatus.PAID && apartment.getPaymentStatus() != devs.group5.rms.entities.PaymentStatus.PAID) {
                val existingPayment = paymentRepository.findByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                        apartment.getId(), devs.group5.rms.entities.PaymentType.RENT, currentYear, currentMonth);
                
                if (existingPayment.isEmpty()) {
                    val payment = devs.group5.rms.entities.Payment.builder()
                            .apartment(apartment)
                            .amount(apartment.getRent())
                            .paymentDate(java.time.LocalDate.now())
                            .billingMonth(currentMonth)
                            .billingYear(currentYear)
                            .type(devs.group5.rms.entities.PaymentType.RENT)
                            .isCancelled(false)
                            .build();
                    paymentRepository.save(payment);
                }

                // Generate MAINTENANCE_FEE payments for the month
                for (val fee : apartment.getMaintenanceFees()) {
                    val existingFeePayment = paymentRepository.findAllByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                            apartment.getId(), devs.group5.rms.entities.PaymentType.MAINTENANCE_FEE, currentYear, currentMonth)
                            .stream().filter(p -> p.getAmount().compareTo(fee.getAmount()) == 0).findFirst();
                    
                    if (existingFeePayment.isEmpty()) {
                        val feePayment = devs.group5.rms.entities.Payment.builder()
                                .apartment(apartment)
                                .amount(fee.getAmount())
                                .paymentDate(java.time.LocalDate.now())
                                .billingMonth(currentMonth)
                                .billingYear(currentYear)
                                .type(devs.group5.rms.entities.PaymentType.MAINTENANCE_FEE)
                                .isCancelled(false)
                                .build();
                        paymentRepository.save(feePayment);
                    }
                }
            } else if (request.paymentStatus() != devs.group5.rms.entities.PaymentStatus.PAID && apartment.getPaymentStatus() == devs.group5.rms.entities.PaymentStatus.PAID) {
                val existingPayment = paymentRepository.findByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                        apartment.getId(), devs.group5.rms.entities.PaymentType.RENT, currentYear, currentMonth);
                
                existingPayment.ifPresent(payment -> {
                    payment.setCancelled(true);
                    paymentRepository.save(payment);
                });

                // Cancel MAINTENANCE_FEE payments for the month
                val existingFeePayments = paymentRepository.findAllByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                        apartment.getId(), devs.group5.rms.entities.PaymentType.MAINTENANCE_FEE, currentYear, currentMonth);
                for (val feePayment : existingFeePayments) {
                    feePayment.setCancelled(true);
                    paymentRepository.save(feePayment);
                }
            }
            apartment.setPaymentStatus(request.paymentStatus());
        }

        return apartmentRepository.save(apartment);
    }

    @Transactional
    public void bulkUpdateApartments(UUID authenticatedUserId, Role authenticatedUserRole, devs.group5.rms.dtos.ApartmentBulkUpdateRequest request) {
        if (request.apartmentIds() == null || request.apartmentIds().isEmpty()) {
            return;
        }

        val apartments = apartmentRepository.findAllById(request.apartmentIds());
        for (val apartment : apartments) {
            ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

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
    public devs.group5.rms.entities.Apartment addSingleApartment(
            UUID authenticatedUserId,
            Role authenticatedUserRole,
            devs.group5.rms.dtos.SingleApartmentCreateRequest request
    ) {
        val property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, property.getOwner().getId());

        val apartment = devs.group5.rms.entities.Apartment.builder()
                .property(property)
                .floor(request.floor())
                .number(request.number())
                .rent(request.rent())
                .squareMeters(request.squareMeters())
                .paymentStatus(devs.group5.rms.entities.PaymentStatus.PAID)
                .build();

        return apartmentRepository.save(apartment);
    }

    @Transactional
    public void deleteApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        if (apartment.isDeleted()) {
            return;
        }

        apartment.setDeleted(true);
        apartmentRepository.save(apartment);
    }

    // ─── Tenant management ───────────────────────────────────────────────────

    /**
     * Assigns a tenant to an apartment. If a tenant with the same name already
     * exists in the DB, reuses it. Otherwise creates a new one.
     */
    @Transactional
    public TenantData assignOrCreateTenant(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, TenantRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

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
    public TenantData updateTenant(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, TenantRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

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
    public void vacateApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        val tenant = apartment.getTenant();
        if (tenant == null) {
            return; // already vacant, nothing to do
        }

        apartment.setTenant(null);
        apartment.setDueDate(null);
        apartmentRepository.save(apartment);

        // If tenant has no more apartments, remove them from DB entirely
        val remainingApartments = apartmentRepository.findByTenant_IdAndIsDeletedFalse(tenant.getId());
        if (remainingApartments.isEmpty()) {
            tenantRepository.delete(tenant);
        }
    }

    // ─── Expense management ──────────────────────────────────────────────────

    public List<ApartmentExpenseData> getExpensesForApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        return expenseRepository.findByApartment_Id(apartmentId).stream()
                .map(e -> new ApartmentExpenseData(e.getId(), e.getAmount(), e.getDescription()))
                .toList();
    }

    @Transactional
    public ApartmentExpenseData addExpenseToApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, ApartmentExpenseRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        val expense = Expense.builder()
                .amount(request.amount())
                .description(request.description())
                .apartment(apartment)
                .build();

        val saved = expenseRepository.save(expense);

        val payment = devs.group5.rms.entities.Payment.builder()
                .apartment(apartment)
                .amount(saved.getAmount())
                .paymentDate(java.time.LocalDate.now())
                .billingMonth(java.time.LocalDate.now().getMonthValue())
                .billingYear(java.time.LocalDate.now().getYear())
                .type(devs.group5.rms.entities.PaymentType.EXPENSE)
                .isCancelled(false)
                .build();
        paymentRepository.save(payment);

        return new ApartmentExpenseData(saved.getId(), saved.getAmount(), saved.getDescription());
    }

    @Transactional
    public void deleteExpenseFromApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, UUID expenseId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        val expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (!expense.getApartment().getId().equals(apartmentId)) {
            throw new IllegalArgumentException("Expense does not belong to this apartment");
        }

        expenseRepository.delete(expense);
    }

    // ─── Maintenance Fee management ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MaintenanceFeeData> getMaintenanceFeesForApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        return maintenanceFeeRepository.findByApartment_Id(apartmentId).stream()
                .map(f -> new MaintenanceFeeData(f.getId(), f.getCategory(), f.getDescription(), f.getAmount()))
                .toList();
    }

    @Transactional
    public MaintenanceFeeData addMaintenanceFeeToApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, MaintenanceFeeRequest request) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        val fee = MaintenanceFee.builder()
                .category(request.category())
                .description(request.description())
                .amount(request.amount())
                .apartment(apartment)
                .build();

        val saved = maintenanceFeeRepository.save(fee);

        return new MaintenanceFeeData(saved.getId(), saved.getCategory(), saved.getDescription(), saved.getAmount());
    }

    @Transactional
    public void deleteMaintenanceFeeFromApartment(UUID authenticatedUserId, Role authenticatedUserRole, UUID apartmentId, UUID maintenanceFeeId) {
        val apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, apartment.getProperty().getOwner().getId());

        val fee = maintenanceFeeRepository.findById(maintenanceFeeId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance fee not found"));

        if (!fee.getApartment().getId().equals(apartmentId)) {
            throw new IllegalArgumentException("Maintenance fee does not belong to this apartment");
        }

        maintenanceFeeRepository.delete(fee);
    }
}
