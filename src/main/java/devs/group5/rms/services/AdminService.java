package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.PropertyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {
    private final AdminRepository adminRepository;
    private final PropertyRepository propertyRepository;

    // Returns available admins so owners can choose who to associate with.
    public List<Admin> getAdmins() {
        return adminRepository.findAll();
    }

    // Returns all owners linked to the authenticated admin
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Owner> getAdminOwners(@NonNull UUID authenticatedAdminId) {
        val admin = adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        return admin.getOwners().stream().toList();
    }

    // Returns properties for a specific owner, validating that the admin manages this owner
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Property> getOwnerProperties(@NonNull UUID authenticatedAdminId, @NonNull UUID ownerId) {
        val admin = adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        // Validate that this admin manages the requested owner
        boolean hasOwner = admin.getOwners().stream()
                .anyMatch(owner -> owner.getId().equals(ownerId));

        if (!hasOwner) {
            throw new RuntimeException("Admin does not manage this owner");
        }

        return propertyRepository.findByOwner_Id(ownerId);
    }
}
