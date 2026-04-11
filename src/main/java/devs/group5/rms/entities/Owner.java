package devs.group5.rms.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "owners")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Owner extends User {
    @JoinColumn(name = "admin_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Admin admin;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Property> properties;

    @Builder
    public Owner(UUID id, String name, Admin admin, List<Property> properties) {
        super(id, name);
        this.admin = admin;
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Owner owner = (Owner) o;
        return super.equals(owner);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
