package devs.group5.rms.dtos;

import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String phone,
        String email
) {
}
