package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentData(int number, UUID propertyId, BigDecimal rent) {
}
