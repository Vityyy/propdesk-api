package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record SingleApartmentCreateRequest(
        UUID propertyId,
        int floor,
        int number,
        BigDecimal rent,
        BigDecimal squareMeters
) {
}
