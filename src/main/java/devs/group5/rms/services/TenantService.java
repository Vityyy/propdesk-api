package devs.group5.rms.services;

import devs.group5.rms.entities.Role;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class TenantService {
    private final TenantRepository tenantRepository;
    private final ApartmentRepository apartmentRepository;
    private final OwnerRepository ownerRepository;

    @Transactional
    public void deleteTenant(UUID authenticatedUserId, Role authenticatedUserRole, UUID tenantId, UUID ownerId) {
        val tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        // Ensure the user has access to the apartments of the tenant
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, ownerId);

        val apartments = apartmentRepository.findByTenant_Id(tenantId);
        for (val apartment : apartments) {
            val aptOwner = apartment.getProperty().getOwner();

            //skip apartments that do not belong to the specified owner
            if (aptOwner == null || !aptOwner.getId().equals(ownerId)) {
                continue;
            }

            apartment.setTenant(null);
            apartment.setDueDate(null);
            apartmentRepository.save(apartment);
        }

        val remaining = apartmentRepository.findActiveByTenantId(tenantId);
        if (remaining.isEmpty()) {
            tenantRepository.delete(tenant);
        }
    }

    private void ensureCanManageOwner(UUID authenticatedUserId, Role authenticatedUserRole, UUID ownerId) {
        if (authenticatedUserRole == Role.OWNER) {
            if (!authenticatedUserId.equals(ownerId)) {
                throw new IllegalArgumentException("Owner cannot access another owner resources");
            }
            return;
        }

        // ADMIN
        if (!ownerRepository.existsByIdAndAdmin_Id(ownerId, authenticatedUserId)) {
            throw new IllegalArgumentException("Admin does not manage this owner");
        }
    }
}
