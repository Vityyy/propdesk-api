package devs.group5.rms.services;

import devs.group5.rms.data.PropertyData;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;

    @PreAuthorize("hasRole('OWNER') and #data.ownerId().toString() == authentication.name")
    public Property addProperty(PropertyData data) {
        var owner = ownerRepository.findById(data.ownerId()).orElseThrow(() -> new RuntimeException(""));
        var property = Property.builder()
                .name(data.name())
                .address(data.address())
                .owner(owner)
                .build();

        propertyRepository.save(property);
        return property;
    }
}
