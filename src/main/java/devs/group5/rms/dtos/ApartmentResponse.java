package devs.group5.rms.dtos;

import java.util.UUID;

public record ApartmentResponse(UUID id, int number, UUID propertyId) {
}
