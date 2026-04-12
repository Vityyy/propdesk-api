package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @Builder)
@Table(name = "expenses", uniqueConstraints = @UniqueConstraint(columnNames = {"description", "property_id"}))
public class Expense {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, name = "category")
    private String category;

    @NotBlank
    @Column(nullable = false, name = "description")
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, name = "amount")
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false, name = "date")
    private BigDecimal date;

    @NotNull
    @Column(nullable = false, name = "payment_status")
    private PaymentStatus paymentStatus;

    @NotNull
    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}
