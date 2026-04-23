package devs.group5.rms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Positive;

@Embeddable
public record ApartmentGroupRange(
        @Positive
        @Column(name = "from_apartment_number", nullable = false)
        Integer fromApartmentNumber,

        @Positive
        @Column(name = "to_apartment_number", nullable = false)
        Integer toApartmentNumber
) {
}
