package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight summary returned by GET /properties/{id}/summary.
 * The frontend uses this to display Monthly Revenue without fetching
 * the full apartment grid.
 */
public record PropertySummaryResponse(
        UUID propertyId,
        int totalApartments,
        int occupiedApartments,
        BigDecimal paidRentTotal,   // sum of rent for apartments with PAID status
        BigDecimal expensesTotal    // sum of all apartment expenses for this property
) {
}
