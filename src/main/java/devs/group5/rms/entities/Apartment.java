package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @Builder)
@Table(name = "apartments", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "property_id"}))
public class Apartment {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @JoinColumn(name = "property_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Property property;

    @JoinColumn(name = "tenant_id")
    @OneToOne(mappedBy = "apartment")
    private Tenant tenant;
}
