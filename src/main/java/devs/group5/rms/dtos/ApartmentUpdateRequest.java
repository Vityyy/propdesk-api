package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import devs.group5.rms.entities.PaymentStatus;

public record ApartmentUpdateRequest(
        BigDecimal rent,
        BigDecimal squareMeters,
        LocalDate dueDate,
        PaymentStatus paymentStatus
) {
}
