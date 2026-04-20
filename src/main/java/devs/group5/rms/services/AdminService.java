package devs.group5.rms.services;

import devs.group5.rms.data.ExpenseData;
import devs.group5.rms.entities.Admin;
import devs.group5.rms.entities.Expense;
import devs.group5.rms.entities.PaymentStatus;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.ExpenseRepository;
import devs.group5.rms.repositories.PropertyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {
    private final AdminRepository adminRepository;
    private final PropertyRepository propertyRepository;
    private final ExpenseRepository expenseRepository;

    // Returns available admins so owners can choose who to associate with.
    public List<Admin> getAdmins() {
        return adminRepository.findAll();
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
