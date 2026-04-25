package devs.group5.rms.dtos;

import devs.group5.rms.entities.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ApartmentWithTenantResponse(
        UUID id,
        LocalDate dueDate,
        PaymentStatus paymentStatus,
        BigDecimal squareMeters,
        BigDecimal rent,
        TenantResponse tenant
) {

}
