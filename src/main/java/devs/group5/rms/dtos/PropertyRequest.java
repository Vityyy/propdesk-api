package devs.group5.rms.dtos;

import java.util.UUID;

public record PropertyRequest(String name, String address, UUID ownerId) {
}
