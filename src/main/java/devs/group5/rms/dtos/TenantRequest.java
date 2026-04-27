package devs.group5.rms.dtos;

public record TenantRequest(
        String name,
        String phone,
        String email
) {
}
