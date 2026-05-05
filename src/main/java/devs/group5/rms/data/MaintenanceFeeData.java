package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceFeeData(
        UUID id,
        String category,
        String description,
        BigDecimal amount
) {
}
