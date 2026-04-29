package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.OwnerRepository;
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
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;

    // Returns available admins so owners can choose who to associate with.
    public List<Admin> getAdmins() {
        return adminRepository.findAll();
    }

    // Returns all owners linked to the authenticated admin
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Owner> getAdminOwners(@NonNull UUID authenticatedAdminId) {
        adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        return ownerRepository.findByAdmin_IdAndAdminAssociationAcceptedTrue(authenticatedAdminId);
    }

    // Returns owner association requests waiting for the authenticated admin approval.
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Owner> getPendingOwnerRequests(@NonNull UUID authenticatedAdminId) {
        adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        return ownerRepository.findByAdmin_IdAndAdminAssociationAcceptedFalse(authenticatedAdminId);
    }

    // Returns properties for a specific owner, validating that the admin manages this owner
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Property> getOwnerProperties(@NonNull UUID authenticatedAdminId, @NonNull UUID ownerId) {
        val admin = adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        // Validate that this admin manages the requested owner
        if (!ownerRepository.existsByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(ownerId, authenticatedAdminId)) {
            throw new RuntimeException("Admin does not manage this owner");
        }

        return propertyRepository.findByOwner_Id(ownerId);
    }

    // Accepts an owner request that was previously sent to the authenticated admin.
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Owner acceptOwnerRequest(
            @NonNull UUID authenticatedAdminId,
            @NonNull UUID ownerId
    ) {
        Admin admin = adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner does not exist"));

        if (owner.getAdmin() == null || !owner.getAdmin().getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Owner did not request association with this admin");
        }

        owner.setAdminAssociationAccepted(true);

        return ownerRepository.save(owner);
    }
}
