package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        UUID apartmentId
) {
}
