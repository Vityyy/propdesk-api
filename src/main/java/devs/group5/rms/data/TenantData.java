package devs.group5.rms.data;

import java.util.UUID;

public record TenantData(
        UUID id,
        String name,
        String phone,
        String email
) {
}
