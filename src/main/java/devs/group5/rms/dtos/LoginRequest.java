package devs.group5.rms.dtos;

public record LoginRequest(
        String email,
        String password
) {
}
