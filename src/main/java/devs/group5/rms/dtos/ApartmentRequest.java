package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import devs.group5.rms.entities.PaymentStatus;

public record ApartmentRequest(int number, UUID propertyId, BigDecimal amount_due, LocalDate dueDate, PaymentStatus paymentStatus) {
}
