package devs.group5.rms.data;

import devs.group5.rms.entities.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ApartmentWithTenantData(
        UUID id,
        int number,
        LocalDate dueDate,
        PaymentStatus paymentStatus,
        int floor,
        BigDecimal squareMeters,
        BigDecimal rent,
        TenantData tenant,
        List<ApartmentExpenseData> expenses
) {
}
