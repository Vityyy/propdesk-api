package devs.group5.rms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "apartments")
public class Apartment {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Positive
    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_status")
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus;

    @NotNull
    @Positive
    @Column(name = "square_meters", nullable = false)
    private BigDecimal squareMeters;

    @Positive
    @Column(name = "floor", nullable = false)
    private int floor;

    @NotNull
    @Positive
    @Column(name = "rent", nullable = false)
    private BigDecimal rent;

    @ToString.Exclude
    @JoinColumn(name = "tenant_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Tenant tenant;

    @ToString.Exclude
    @JoinColumn(name = "property_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Property property;
}
