package devs.group5.rms.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseData(
        String category,
        String description,
        BigDecimal amount,
        LocalDate date,
        UUID propertyId) {
}
