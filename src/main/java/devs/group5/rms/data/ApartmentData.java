package devs.group5.rms.data;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import devs.group5.rms.entities.PaymentStatus;

public record ApartmentData(int number, UUID propertyId, BigDecimal rent, LocalDate dueDate, PaymentStatus paymentStatus) {
}
