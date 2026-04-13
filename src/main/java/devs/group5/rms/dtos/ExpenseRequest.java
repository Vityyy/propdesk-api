package devs.group5.rms.dtos;

import java.math.BigDecimal;

public record ExpenseRequest(String category, String description, BigDecimal amount) {
}
