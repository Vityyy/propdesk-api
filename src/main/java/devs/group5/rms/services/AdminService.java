package devs.group5.rms.services;

import devs.group5.rms.repositories.AdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {
    private final AdminRepository adminRepository;
}
