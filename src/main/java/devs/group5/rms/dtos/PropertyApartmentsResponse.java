package devs.group5.rms.dtos;

import devs.group5.rms.entities.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PropertyApartmentsResponse(
        UUID apartmentId,
        BigDecimal amountDue,
        PaymentStatus paymentStatus,
        UUID tenantId,
        String tenantName,

        // añadí floor a apartment,
        // añadí number a apartment,    (me parece)

        Double mts2,
        LocalDate dueDate
) {
}
