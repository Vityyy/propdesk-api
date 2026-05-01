package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

// Returns the persisted owner-admin association after the owner grants permission.
public record OwnerAdminAssociationResponse(
        UUID ownerId,
        String ownerName,
        UUID adminId,
        String adminName,
        BigDecimal adminCut,
        Boolean associationAccepted
) {
}
