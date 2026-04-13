package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequest(String category, String description, BigDecimal amount, LocalDate date, UUID propertyId) {
}
