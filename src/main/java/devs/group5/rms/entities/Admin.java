package devs.group5.rms.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "admin")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Admin extends User {

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Owner> owners;

    @Builder
    public Admin(UUID id, String name, List<Owner> owners) {
        super(id, name);
        this.owners = owners;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Admin owner = (Admin) o;
        return super.equals(owner);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
