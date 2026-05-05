package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record OwnerAssociationRequestResponse(
        UUID ownerId,
        String ownerName,
        BigDecimal adminCut
) {
}
