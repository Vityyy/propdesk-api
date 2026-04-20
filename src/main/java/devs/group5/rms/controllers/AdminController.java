package devs.group5.rms.controllers;

import devs.group5.rms.dtos.UserResponse;
import devs.group5.rms.services.AdminService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Exposes admin resources used by owners when granting management access.
@RestController
@RequestMapping("/admins")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminController {
    private final AdminService adminService;

    // Lists registered admins so owners can pick one from the UI.
    @GetMapping
    public List<UserResponse> listAdmins() {
        val admins = adminService.getAdmins();

        return admins.stream()
                .map(admin -> new UserResponse(admin.getId(), admin.getName()))
                .toList();
    }
}
