package devs.group5.rms.dtos;

import java.util.UUID;

public record ApartmentResponse(UUID id, String name, UUID propertyId) {
}
