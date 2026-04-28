package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentExpenseResponse(
        UUID id,
        BigDecimal amount,
        String description
) {
}
