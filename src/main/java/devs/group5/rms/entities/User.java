package devs.group5.rms.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, name = "name")
    private String name;

    protected User(@Nullable UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
