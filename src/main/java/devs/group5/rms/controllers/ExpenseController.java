package devs.group5.rms.controllers;

import devs.group5.rms.data.ExpenseData;
import devs.group5.rms.dtos.ExpenseRequest;
import devs.group5.rms.dtos.ExpenseResponse;
import devs.group5.rms.services.AdminService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ExpenseController {
    private final AdminService adminService;

    @PostMapping
    public ExpenseResponse addExpense(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ExpenseRequest request
    ) {
        val response = adminService.addExpense(
                UUID.fromString(jwt.getId()),
                new ExpenseData(
                        request.category(),
                        request.description(),
                        request.amount(),
                        request.date(),
                        request.propertyId()
                )
        );


        return new ExpenseResponse(
                response.getId(),
                response.getCategory(),
                response.getDescription(),
                response.getAmount(),
                response.getDate(),
                response.getProperty().getId()
        );
    }
}
