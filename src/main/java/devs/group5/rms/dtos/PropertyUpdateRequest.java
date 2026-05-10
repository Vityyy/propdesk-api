package devs.group5.rms.dtos;

import java.net.URL;

public record PropertyUpdateRequest(
        String propertyName,
        String propertyAddress,
        URL pictureUrl
) {
}
