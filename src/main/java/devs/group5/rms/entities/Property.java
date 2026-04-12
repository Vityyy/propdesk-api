package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "properties", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "address", "owner_id"}))
public class Property {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank
    @Column(nullable = false, name = "name")
    private String name;
    @NotBlank
    @Column(nullable = false, name = "address")
    private String address;
    @NotNull
    @ToString.Exclude
    @JoinColumn(name = "owner_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Owner owner;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Apartment> apartments;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Expense> expenses;
}
