package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentRequest(String name, UUID propertyId, BigDecimal amount_due) {
}
