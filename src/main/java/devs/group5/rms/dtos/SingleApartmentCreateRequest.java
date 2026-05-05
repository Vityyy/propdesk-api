package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import devs.group5.rms.entities.PaymentStatus;

public record SingleApartmentCreateRequest(
        UUID propertyId,
        int floor,
        int number,
        BigDecimal rent,
        BigDecimal squareMeters,
        LocalDate dueDate,
        PaymentStatus paymentStatus
) {
}
