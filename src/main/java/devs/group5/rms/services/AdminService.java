package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.Expense;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.PaymentStatus;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.ExpenseRepository;
import devs.group5.rms.repositories.PropertyRepository;
import devs.group5.rms.repositories.OwnerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {
    private final AdminRepository adminRepository;
    private final PropertyRepository propertyRepository;
    private final ApartmentRepository apartmentRepository;
    private final ExpenseRepository expenseRepository;
    private final OwnerRepository ownerRepository;

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

    // Returns apartments for a specific owner, validating that the admin manages this owner
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Apartment> getOwnerApartments(@NonNull UUID authenticatedAdminId, @NonNull UUID ownerId) {
        val admin = adminRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new RuntimeException("Admin does not exist"));

        // Validate that this admin manages the requested owner
        boolean hasOwner = admin.getOwners().stream()
                .anyMatch(owner -> owner.getId().equals(ownerId));
        
        if (!hasOwner) {
            throw new RuntimeException("Admin does not manage this owner");
        }

        return apartmentRepository.findByProperty_Owner_Id(ownerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Expense addExpense(@NonNull UUID authenticatedUserId, @NonNull ExpenseData data) {
        val property = propertyRepository.findById(data.propertyId()).orElseThrow(() -> new RuntimeException("Property does not exist"));

        val owner = property.getOwner();
        if (owner.getAdmin() == null || !owner.getAdmin().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Property does not belong to an owner associated with authenticated admin");
        }

        val expense = Expense.builder()
                .category(data.category())
                .description(data.description())
                .amount(data.amount())
                .date(data.date())
                .paymentStatus(PaymentStatus.PENDING)
                .property(property)
                .build();

        return expenseRepository.save(expense);
    }
}
