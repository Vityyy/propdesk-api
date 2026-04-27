package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ApartmentBulkUpdateRequest(
        List<UUID> apartmentIds,
        BigDecimal rent,
        BigDecimal squareMeters
) {
}
