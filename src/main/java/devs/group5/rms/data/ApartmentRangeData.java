package devs.group5.rms.data;

import lombok.val;

import java.math.BigDecimal;
import java.util.stream.IntStream;

public record ApartmentRangeData(
        int startFloor,
        int endFloor,
        int startApartmentNumber,
        int endApartmentNumber,
        BigDecimal squareMeters,
        BigDecimal rentValue
) {
    public ApartmentRangeData {
        if (startFloor > endFloor) {
            throw new IllegalArgumentException(
                    "Cannot instantiate an ApartmentsRangeData with a startFloor greater than the endFloor"
            );
        }

        if (startApartmentNumber > endApartmentNumber) {
            throw new IllegalArgumentException(
                    "Cannot instantiate an ApartmentsRangeData with a startApartmentNumber greater than the endApartmentNumber"
            );
        }
    }

    public boolean overlapsWith(ApartmentRangeData other) {
        val floorOverlapStart = Math.max(startFloor, other.startFloor);
        val floorOverlapEnd = Math.min(endFloor, other.endFloor);

        // Check if floors overlap
        if (floorOverlapStart > floorOverlapEnd) {
            return false;
        }

        // If floors overlap, check if apartment numbers overlap
        val numberOverlapStart = Math.max(startApartmentNumber, other.startApartmentNumber);
        val numberOverlapEnd = Math.min(endApartmentNumber, other.endApartmentNumber);

        // If they overlap, return true, if not, return false
        return numberOverlapStart <= numberOverlapEnd;
    }

    public IntStream floorsAsRange() {
        return IntStream.rangeClosed(startFloor, endFloor);
    }

    public IntStream apartmentNumbersAsRange() {
        return IntStream.rangeClosed(startApartmentNumber, endApartmentNumber);
    }
}
