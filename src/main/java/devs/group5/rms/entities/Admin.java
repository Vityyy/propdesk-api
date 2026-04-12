package devs.group5.rms.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admins")
@ToString(callSuper = true)
public class Admin extends User {

    @ToString.Exclude
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)
    private List<Owner> owners;

    @Builder
    private Admin(UUID id, String name, List<Owner> owners) {
        super(id, name);
        this.owners = owners;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
