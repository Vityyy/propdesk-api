package devs.group5.rms.dtos;

import java.util.UUID;

public record PropertyResponse(UUID id, String name, String address, UUID ownerId) {
}
