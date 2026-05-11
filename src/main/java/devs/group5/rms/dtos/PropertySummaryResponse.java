package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertySummaryResponse(
        UUID propertyId,
        int totalApartments,
        int occupiedApartments,
        BigDecimal paidRentTotal,   // sum of rent for apartments with PAID status
        BigDecimal expensesTotal    // sum of all apartment expenses for this property
) {
}
