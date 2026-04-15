package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "users")
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, name = "name")
    private String name;

    @NotBlank
    @Column(nullable = false, name = "password")
    private String password;

    protected User(UUID id, String name, String password) {
        this.id = id;
        this.name = name;
    }

    abstract public Role getRole();
}
