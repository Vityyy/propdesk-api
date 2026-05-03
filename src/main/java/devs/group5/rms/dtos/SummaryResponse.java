package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.List;

public record SummaryResponse(
        BigDecimal totalCollectedThisMonth,
        String collectedTrend,
        long unpaidTenantsCount,
        long totalTenantsCount,
        BigDecimal unpaidAmount,
        BigDecimal totalExpensesThisMonth,
        String expensesTrend,
        SummaryBreakdownData monthlyBreakdown,
        List<MonthlySummaryData> historicalData
) {
    public record SummaryBreakdownData(
            BigDecimal grossRevenue,
            BigDecimal totalExpenses,
            BigDecimal adminCommission,
            BigDecimal netProfit
    ) {}

    public record MonthlySummaryData(
            String month,
            BigDecimal revenue,
            BigDecimal profit
    ) {}
}
