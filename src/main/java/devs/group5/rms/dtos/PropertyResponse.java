package devs.group5.rms.dtos;

import java.net.URL;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        String name,
        String address,
        URL imageUrl,
        UUID ownerId
) {
}
