package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseData(
        String description,
        BigDecimal amount,
        UUID apartmentId) {
}
