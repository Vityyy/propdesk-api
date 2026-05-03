package devs.group5.rms.services;

import devs.group5.rms.dtos.SummaryResponse;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Payment;
import devs.group5.rms.entities.PaymentStatus;
import devs.group5.rms.entities.PaymentType;
import devs.group5.rms.entities.Role;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class SummaryService {
    private final PaymentRepository paymentRepository;
    private final ApartmentRepository apartmentRepository;
    private final OwnerRepository ownerRepository;

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(UUID authenticatedUserId, Role authenticatedUserRole, UUID ownerId) {
        ensureCanManageOwner(authenticatedUserId, authenticatedUserRole, ownerId);

        val owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        val now = LocalDate.now();
        val startOfThisMonth = now.withDayOfMonth(1);
        val endOfThisMonth = now.withDayOfMonth(now.lengthOfMonth());
        val startOfLastMonth = startOfThisMonth.minusMonths(1);
        val endOfLastMonth = startOfThisMonth.minusDays(1);

        val allPayments = paymentRepository.findByOwnerId(ownerId);
        
        // This Month
        val thisMonthPayments = allPayments.stream()
                .filter(p -> !p.getPaymentDate().isBefore(startOfThisMonth) && !p.getPaymentDate().isAfter(endOfThisMonth))
                .toList();

        BigDecimal collectedThisMonth = sumAmount(thisMonthPayments, PaymentType.RENT);
        BigDecimal expensesThisMonth = sumAmount(thisMonthPayments, PaymentType.EXPENSE)
                .add(sumAmount(thisMonthPayments, PaymentType.MAINTENANCE_FEE));

        // Last Month
        val lastMonthPayments = allPayments.stream()
                .filter(p -> !p.getPaymentDate().isBefore(startOfLastMonth) && !p.getPaymentDate().isAfter(endOfLastMonth))
                .toList();

        BigDecimal collectedLastMonth = sumAmount(lastMonthPayments, PaymentType.RENT);
        BigDecimal expensesLastMonth = sumAmount(lastMonthPayments, PaymentType.EXPENSE)
                .add(sumAmount(lastMonthPayments, PaymentType.MAINTENANCE_FEE));

        // Trends
        String collectedTrend = calculateTrend(collectedLastMonth, collectedThisMonth);
        String expensesTrend = calculateTrend(expensesLastMonth, expensesThisMonth);

        // Current Tenants Status
        val apartments = apartmentRepository.findByProperty_Owner_Id(ownerId);
        
        long totalTenantsCount = 0;
        long unpaidTenantsCount = 0;
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (val apt : apartments) {
            if (apt.getTenant() != null) {
                totalTenantsCount++;
                if (apt.getPaymentStatus() == PaymentStatus.PENDING || apt.getPaymentStatus() == PaymentStatus.OVERDUE) {
                    unpaidTenantsCount++;
                    unpaidAmount = unpaidAmount.add(apt.getRent());
                }
            }
        }

        // Monthly Breakdown (Current Month)
        BigDecimal adminCommissionPct = owner.getAdminCut() != null ? owner.getAdminCut() : BigDecimal.ZERO;
        BigDecimal adminCommission = collectedThisMonth.multiply(adminCommissionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netProfit = collectedThisMonth.subtract(expensesThisMonth).subtract(adminCommission);

        val breakdown = new SummaryResponse.SummaryBreakdownData(
                collectedThisMonth,
                expensesThisMonth,
                adminCommission,
                netProfit
        );

        // Historical Data (Last 6 months including current)
        List<SummaryResponse.MonthlySummaryData> historicalData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            
            val monthPayments = allPayments.stream()
                    .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
                    .toList();
            
            BigDecimal monthRev = sumAmount(monthPayments, PaymentType.RENT);
            BigDecimal monthExp = sumAmount(monthPayments, PaymentType.EXPENSE)
                    .add(sumAmount(monthPayments, PaymentType.MAINTENANCE_FEE));
            BigDecimal monthComm = monthRev.multiply(adminCommissionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal monthProfit = monthRev.subtract(monthExp).subtract(monthComm);
            
            historicalData.add(new SummaryResponse.MonthlySummaryData(
                    ym.format(formatter),
                    monthRev,
                    monthProfit
            ));
        }

        return new SummaryResponse(
                collectedThisMonth,
                collectedTrend,
                unpaidTenantsCount,
                totalTenantsCount,
                unpaidAmount,
                expensesThisMonth,
                expensesTrend,
                breakdown,
                historicalData
        );
    }

    private BigDecimal sumAmount(List<Payment> payments, PaymentType type) {
        return payments.stream()
                .filter(p -> p.getType() == type)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String calculateTrend(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return "0% from last month";
            }
            return "+100% from last month";
        }
        
        BigDecimal diff = current.subtract(previous);
        BigDecimal pct = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        
        String sign = pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%.1f%% from last month", sign, pct);
    }

    private void ensureCanManageOwner(UUID authenticatedUserId, Role authenticatedUserRole, UUID ownerId) {
        if (authenticatedUserRole == Role.OWNER) {
            if (!authenticatedUserId.equals(ownerId)) {
                throw new IllegalArgumentException("Owner cannot access another owner resources");
            }
            return;
        }

        if (!ownerRepository.existsByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(ownerId, authenticatedUserId)) {
            throw new IllegalArgumentException("Admin does not manage this owner");
        }
    }
}
