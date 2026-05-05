package devs.group5.rms.services;

import devs.group5.rms.dtos.SummaryResponse;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Payment;
import devs.group5.rms.entities.PaymentStatus;
import devs.group5.rms.entities.PaymentType;
import devs.group5.rms.entities.Role;
import devs.group5.rms.entities.Tenant;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SummaryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSummary() {
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(ownerId)
                .build();
        owner.setAdminCut(new BigDecimal("10.00"));

        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(ownerRepository.existsByIdAndAdmin_IdAndAdminAssociationAcceptedTrue(any(), any())).thenReturn(true);

        LocalDate now = LocalDate.now();

        Payment rentPayment = Payment.builder()
                .amount(new BigDecimal("1000"))
                .paymentDate(now)
                .type(PaymentType.RENT)
                .build();

        Payment expensePayment = Payment.builder()
                .amount(new BigDecimal("200"))
                .paymentDate(now)
                .type(PaymentType.EXPENSE)
                .build();

        when(paymentRepository.findByOwnerId(ownerId)).thenReturn(List.of(rentPayment, expensePayment));

        Apartment paidApt = Apartment.builder()
                .paymentStatus(PaymentStatus.PAID)
                .tenant(Tenant.builder().build())
                .rent(new BigDecimal("1000"))
                .build();

        Apartment pendingApt = Apartment.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .tenant(Tenant.builder().build())
                .rent(new BigDecimal("1000"))
                .build();

        when(apartmentRepository.findByProperty_Owner_IdAndIsDeletedFalse(ownerId)).thenReturn(List.of(paidApt, pendingApt));

        SummaryResponse response = summaryService.getSummary(ownerId, Role.OWNER, ownerId);

        assertEquals(new BigDecimal("1000"), response.totalCollectedThisMonth());
        assertEquals(new BigDecimal("200"), response.totalExpensesThisMonth());
        assertEquals(2, response.totalTenantsCount());
        assertEquals(1, response.unpaidTenantsCount());
        assertEquals(new BigDecimal("1000"), response.unpaidAmount());

        assertEquals(new BigDecimal("1000"), response.monthlyBreakdown().grossRevenue());
        assertEquals(new BigDecimal("200"), response.monthlyBreakdown().totalExpenses());
        assertEquals(new BigDecimal("100.00"), response.monthlyBreakdown().adminCommission()); // 10% of 1000
        assertEquals(new BigDecimal("700.00"), response.monthlyBreakdown().netProfit()); // 1000 - 200 - 100
    }
}
