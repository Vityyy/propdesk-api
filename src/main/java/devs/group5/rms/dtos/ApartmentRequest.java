package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ApartmentRequest(int number, UUID propertyId, BigDecimal amount_due) {
}
