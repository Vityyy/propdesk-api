package devs.group5.rms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
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

    @Positive
    @NotNull
    @Column(name = "admin_cut", nullable = false)
    private BigDecimal adminCut;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Property> properties;

    @Builder
    private Owner(UUID id, String name, Admin admin, List<Property> properties) {
        super(id, name);
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
}
