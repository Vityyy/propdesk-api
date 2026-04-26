package devs.group5.rms.dtos;

import devs.group5.rms.data.ApartmentRangeData;

import java.util.List;
import java.util.UUID;

public record PropertyRequest(
        String propertyName,
        String propertyAddress,
        String pictureUrl,
        UUID ownerId,
        List<ApartmentRangeData> apartmentRanges
) {
}
