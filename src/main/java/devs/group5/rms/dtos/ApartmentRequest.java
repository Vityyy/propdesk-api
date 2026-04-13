package devs.group5.rms.dtos;

import java.util.UUID;

public record ApartmentRequest(String name, UUID propertyId) {
}
