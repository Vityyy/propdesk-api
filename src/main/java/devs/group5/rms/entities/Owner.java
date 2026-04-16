package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "owners")
@ToString(callSuper = true)
public class Owner extends User {
    @ToString.Exclude
    @JoinColumn(name = "admin_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Admin admin;

    @Positive
    @Column(name = "admin_cut")
    private BigDecimal adminCut;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Property> properties;

    @Builder
    private Owner(UUID id, String name, String password, Admin admin, List<Property> properties) {
        super(id, name, password);
        this.admin = admin;
        this.properties = properties;
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
        return Role.OWNER;
    }
}
