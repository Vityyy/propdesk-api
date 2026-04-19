package devs.group5.rms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

// Carries the admin selected by the owner and the agreed management cut.
public record AssociateAdminRequest(UUID adminId, BigDecimal adminCut) {
}
