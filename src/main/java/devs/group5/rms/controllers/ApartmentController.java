package devs.group5.rms.controllers;

import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.dtos.*;
import devs.group5.rms.services.OwnerService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apartments")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentController {
    private final OwnerService ownerService;
    private final devs.group5.rms.services.ApartmentService apartmentService;

    @PostMapping
    public List<ApartmentResponse> addApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<ApartmentRequest> requests
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());

        return requests.stream()
                .map(r -> {
                    val apartment = ownerService.addApartment(
                            ownerId,
                            new ApartmentData(r.number(), r.propertyId(), r.amount_due())
                    );
                    return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<ApartmentResponse> getApartments(@AuthenticationPrincipal Jwt jwt) {
        val ownerId = UUID.fromString(jwt.getSubject());
        return ownerService.getApartments(ownerId)
                .stream()
                .map(r -> new ApartmentResponse(r.getId(), r.getNumber(), r.getProperty().getId()))
                .collect(Collectors.toList());
    }

    @PutMapping("/{apartmentId}")
    public ApartmentResponse updateApartment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody ApartmentUpdateRequest request
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());
        val apartment = apartmentService.updateApartment(ownerId, apartmentId, request);
        return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
    }

    @PutMapping("/bulk")
    public void bulkUpdateApartments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApartmentBulkUpdateRequest request
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());
        apartmentService.bulkUpdateApartments(ownerId, request);
    }

    @PostMapping("/single")
    public ApartmentResponse addSingleApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SingleApartmentCreateRequest request
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());
        val apartment = apartmentService.addSingleApartment(ownerId, request);
        return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
    }

    @DeleteMapping("/{apartmentId}")
    public void deleteApartment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());
        apartmentService.deleteApartment(ownerId, apartmentId);
    }

    // ─── Tenant endpoints ────────────────────────────────────────────────────

    @PostMapping("/{apartmentId}/tenant")
    public TenantResponse assignTenant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody TenantRequest request
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        val tenantData = apartmentService.assignOrCreateTenant(ownerId, apartmentId, request);
        return new TenantResponse(tenantData.id(), tenantData.name(), tenantData.phone(), tenantData.email());
    }

    @PutMapping("/{apartmentId}/tenant")
    public TenantResponse updateTenant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody TenantRequest request
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        val tenantData = apartmentService.updateTenant(ownerId, apartmentId, request);
        return new TenantResponse(tenantData.id(), tenantData.name(), tenantData.phone(), tenantData.email());
    }

    @DeleteMapping("/{apartmentId}/tenant")
    public void vacateApartment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        apartmentService.vacateApartment(ownerId, apartmentId);
    }

    // ─── Expense endpoints ───────────────────────────────────────────────────

    @GetMapping("/{apartmentId}/expenses")
    public List<ApartmentExpenseResponse> getExpenses(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        return apartmentService.getExpensesForApartment(ownerId, apartmentId).stream()
                .map(e -> new ApartmentExpenseResponse(e.id(), e.amount(), e.description()))
                .toList();
    }

    @PostMapping("/{apartmentId}/expenses")
    public ApartmentExpenseResponse addExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody ApartmentExpenseRequest request
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        val data = apartmentService.addExpenseToApartment(ownerId, apartmentId, request);
        return new ApartmentExpenseResponse(data.id(), data.amount(), data.description());
    }

    @DeleteMapping("/{apartmentId}/expenses/{expenseId}")
    public void deleteExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @PathVariable UUID expenseId
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        apartmentService.deleteExpenseFromApartment(ownerId, apartmentId, expenseId);
    }
}
