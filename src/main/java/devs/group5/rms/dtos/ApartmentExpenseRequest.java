package devs.group5.rms.dtos;

import java.math.BigDecimal;

public record ApartmentExpenseRequest(
        BigDecimal amount,
        String description
) {
}
