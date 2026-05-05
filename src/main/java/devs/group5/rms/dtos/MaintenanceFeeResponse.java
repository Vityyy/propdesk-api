package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceFeeResponse(
        UUID id,
        String category,
        String description,
        BigDecimal amount
) {
}
