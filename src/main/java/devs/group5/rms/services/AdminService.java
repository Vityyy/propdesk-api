package devs.group5.rms.services;

import devs.group5.rms.entities.Admin;
import devs.group5.rms.repositories.AdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {
    private final AdminRepository adminRepository;

    // Returns available admins so owners can choose who to associate with.
    public List<Admin> getAdmins() {
        return adminRepository.findAll();
    }
}
