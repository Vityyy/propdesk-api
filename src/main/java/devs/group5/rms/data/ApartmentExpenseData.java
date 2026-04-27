package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentExpenseData(
        UUID id,
        BigDecimal amount,
        String description
) {
}
