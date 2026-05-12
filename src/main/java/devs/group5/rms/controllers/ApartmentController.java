package devs.group5.rms.controllers;

import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.dtos.*;
import devs.group5.rms.entities.Role;
import devs.group5.rms.services.JwtService;
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
    private final devs.group5.rms.services.ApartmentService apartmentService;
    private final JwtService jwtService;

    @PostMapping
    public List<ApartmentResponse> addApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<ApartmentRequest> requests
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());

        return requests.stream()
                .map(r -> {
                    val apartment = apartmentService.addApartment(
                            authenticatedUserId,
                            authenticatedUserRole,
                            new ApartmentData(r.number(), r.propertyId(), r.amount_due(), r.dueDate(), r.paymentStatus())
                    );
                    return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<ApartmentResponse> getApartments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "ownerId", required = false) String ownerIdParam
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        UUID targetOwnerId;
        if (authenticatedUserRole == Role.ADMIN) {
            if (ownerIdParam == null || ownerIdParam.isBlank()) {
                throw new IllegalArgumentException("Admin must provide ownerId parameter");
            }
            targetOwnerId = UUID.fromString(ownerIdParam);
        } else {
            targetOwnerId = authenticatedUserId;
        }

        return apartmentService.getApartmentsForUser(authenticatedUserId, authenticatedUserRole, targetOwnerId)
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
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val apartment = apartmentService.updateApartment(authenticatedUserId, authenticatedUserRole, apartmentId, request);
        return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
    }

    @PutMapping("/bulk")
    public void bulkUpdateApartments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApartmentBulkUpdateRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        apartmentService.bulkUpdateApartments(authenticatedUserId, authenticatedUserRole, request);
    }

    @PostMapping("/single")
    public ApartmentResponse addSingleApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SingleApartmentCreateRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val apartment = apartmentService.addSingleApartment(authenticatedUserId, authenticatedUserRole, request);
        return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
    }

    @DeleteMapping("/{apartmentId}")
    public void deleteApartment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        apartmentService.deleteApartment(authenticatedUserId, authenticatedUserRole, apartmentId);
    }


    // Tenant endpoints

    @PostMapping("/{apartmentId}/tenant")
    public TenantResponse assignTenant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody TenantRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val tenantData = apartmentService.assignOrCreateTenant(authenticatedUserId, authenticatedUserRole, apartmentId, request);
        return new TenantResponse(tenantData.id(), tenantData.name(), tenantData.phone(), tenantData.email());
    }

    @PutMapping("/{apartmentId}/tenant")
    public TenantResponse updateTenant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody TenantRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val tenantData = apartmentService.updateTenant(authenticatedUserId, authenticatedUserRole, apartmentId, request);
        return new TenantResponse(tenantData.id(), tenantData.name(), tenantData.phone(), tenantData.email());
    }

    @DeleteMapping("/{apartmentId}/tenant")
    public void vacateApartment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        apartmentService.vacateApartment(authenticatedUserId, authenticatedUserRole, apartmentId);
    }


    // Expense endpoints

    @GetMapping("/{apartmentId}/expenses")
    public List<ApartmentExpenseResponse> getExpenses(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        return apartmentService.getExpensesForApartment(authenticatedUserId, authenticatedUserRole, apartmentId).stream()
                .map(e -> new ApartmentExpenseResponse(e.id(), e.amount(), e.description()))
                .toList();
    }

    @PostMapping("/{apartmentId}/expenses")
    public ApartmentExpenseResponse addExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody ApartmentExpenseRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val data = apartmentService.addExpenseToApartment(authenticatedUserId, authenticatedUserRole, apartmentId, request);
        return new ApartmentExpenseResponse(data.id(), data.amount(), data.description());
    }

    @DeleteMapping("/{apartmentId}/expenses/{expenseId}")
    public void deleteExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @PathVariable UUID expenseId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        apartmentService.deleteExpenseFromApartment(authenticatedUserId, authenticatedUserRole, apartmentId, expenseId);
    }


    // Maintenance Fee endpoints

    @GetMapping("/{apartmentId}/maintenance-fees")
    public List<MaintenanceFeeResponse> getMaintenanceFees(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        return apartmentService.getMaintenanceFeesForApartment(authenticatedUserId, authenticatedUserRole, apartmentId).stream()
                .map(f -> new MaintenanceFeeResponse(f.id(), f.category(), f.description(), f.amount()))
                .toList();
    }

    @PostMapping("/{apartmentId}/maintenance-fees")
    public MaintenanceFeeResponse addMaintenanceFee(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @RequestBody MaintenanceFeeRequest request
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val data = apartmentService.addMaintenanceFeeToApartment(authenticatedUserId, authenticatedUserRole, apartmentId, request);
        return new MaintenanceFeeResponse(data.id(), data.category(), data.description(), data.amount());
    }

    @DeleteMapping("/{apartmentId}/maintenance-fees/{maintenanceFeeId}")
    public void deleteMaintenanceFee(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apartmentId,
            @PathVariable UUID maintenanceFeeId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        apartmentService.deleteMaintenanceFeeFromApartment(authenticatedUserId, authenticatedUserRole, apartmentId, maintenanceFeeId);
    }
}
