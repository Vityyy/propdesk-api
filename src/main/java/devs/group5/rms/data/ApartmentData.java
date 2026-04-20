package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentData(String name, UUID propertyId, BigDecimal amount_due) {
}
