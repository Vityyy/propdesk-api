package devs.group5.rms.dtos;

import java.math.BigDecimal;

public record ApartmentUpdateRequest(
        BigDecimal rent,
        BigDecimal squareMeters
) {
}
