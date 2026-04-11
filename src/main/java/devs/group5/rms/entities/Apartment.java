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
@Table(name = "apartments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @Builder)
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

    @OneToOne(mappedBy = "apartment")
    private Tenant tenant;
}
