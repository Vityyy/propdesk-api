package devs.group5.rms.stepdefinitions;

import devs.group5.rms.dtos.ApartmentUpdateRequest;
import devs.group5.rms.dtos.MaintenanceFeeRequest;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.MaintenanceFee;
import devs.group5.rms.entities.Owner;
import devs.group5.rms.entities.Payment;
import devs.group5.rms.entities.PaymentStatus;
import devs.group5.rms.entities.PaymentType;
import devs.group5.rms.entities.Property;
import devs.group5.rms.entities.Role;
import devs.group5.rms.entities.User;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.MaintenanceFeeRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PaymentRepository;
import devs.group5.rms.repositories.PropertyRepository;
import devs.group5.rms.repositories.UserRepository;
import devs.group5.rms.services.ApartmentService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MaintenanceFeesSteps {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private MaintenanceFeeRepository maintenanceFeeRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ApartmentService apartmentService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User authUser;
    private Apartment testApartment;

    @Given("the database is initialized for maintenance fees testing")
    public void theDatabaseIsInitializedForMaintenanceFeesTesting() {
        // Do not delete all to avoid breaking other tests running in the same context
    }

    @And("an authenticated owner user {string} exists")
    public void anAuthenticatedOwnerUserExists(String email) {
        Owner owner = Owner.builder()
                .name(email) // name acts as email here
                .password(passwordEncoder.encode("password"))
                .build();
        authUser = ownerRepository.save(owner);

        Property property = Property.builder()
                .name("Test Property")
                .address("123 Test St")
                .owner((Owner) authUser)
                .build();
        propertyRepository.save(property);
    }

    private Apartment createApartment(int number, PaymentStatus status) {
        Property property = propertyRepository.findAll().stream()
                .filter(p -> p.getOwner().getId().equals(authUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Property not found for authUser"));
        Apartment apt = Apartment.builder()
                .number(number)
                .floor(1)
                .rent(new BigDecimal("1000.00"))
                .squareMeters(new BigDecimal("50.00"))
                .paymentStatus(status)
                .property(property)
                .build();
        return apartmentRepository.save(apt);
    }

    @When("the user {string} assigns a maintenance fee of {double} with category {string} to apartment {int}")
    public void theUserAssignsAMaintenanceFeeToApartment(String email, double amount, String category, int aptNumber) {
        testApartment = createApartment(aptNumber, PaymentStatus.PENDING);
        
        MaintenanceFeeRequest req = new MaintenanceFeeRequest(category, "Test Fee", new BigDecimal(String.valueOf(amount)));
        apartmentService.addMaintenanceFeeToApartment(authUser.getId(), Role.OWNER, testApartment.getId(), req);
    }

    @Then("the maintenance fee is successfully assigned to apartment {int}")
    @Transactional
    public void theMaintenanceFeeIsSuccessfullyAssignedToApartment(int aptNumber) {
        Apartment apt = apartmentRepository.findById(testApartment.getId()).orElseThrow();
        assertEquals(1, apt.getMaintenanceFees().size());
    }

    @Given("apartment {int} has a maintenance fee of {double} with category {string}")
    public void apartmentHasAMaintenanceFeeOfWithCategory(int aptNumber, double amount, String category) {
        testApartment = createApartment(aptNumber, PaymentStatus.PENDING);
        MaintenanceFee fee = MaintenanceFee.builder()
                .category(category)
                .description("Desc")
                .amount(new BigDecimal(String.valueOf(amount)))
                .apartment(testApartment)
                .build();
        maintenanceFeeRepository.save(fee);
    }

    @And("apartment {int} has a payment status of {string}")
    public void apartmentHasAPaymentStatusOf(int aptNumber, String status) {
        testApartment.setPaymentStatus(PaymentStatus.valueOf(status));
        apartmentRepository.save(testApartment);
    }

    @When("the user {string} marks apartment {int} as {string}")
    public void theUserMarksApartmentAs(String email, int aptNumber, String status) {
        ApartmentUpdateRequest req = new ApartmentUpdateRequest(null, null, null, PaymentStatus.valueOf(status));
        apartmentService.updateApartment(authUser.getId(), Role.OWNER, testApartment.getId(), req);
    }

    @Then("a payment of type {string} for {double} is generated for apartment {int} for the current month")
    public void aPaymentOfTypeForIsGeneratedForApartmentForTheCurrentMonth(String type, double amount, int aptNumber) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        
        List<Payment> payments = paymentRepository.findAllByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                testApartment.getId(), PaymentType.valueOf(type), currentYear, currentMonth);
        
        assertFalse(payments.isEmpty());
        assertEquals(0, new BigDecimal(String.valueOf(amount)).compareTo(payments.get(0).getAmount()));
    }

    @And("a payment of type {string} for {double} exists for apartment {int} for the current month")
    public void aPaymentOfTypeForExistsForApartmentForTheCurrentMonth(String type, double amount, int aptNumber) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        
        Payment payment = Payment.builder()
                .apartment(testApartment)
                .amount(new BigDecimal(String.valueOf(amount)))
                .paymentDate(LocalDate.now())
                .billingMonth(currentMonth)
                .billingYear(currentYear)
                .type(PaymentType.valueOf(type))
                .isCancelled(false)
                .build();
        paymentRepository.save(payment);
    }

    @Then("the payment of type {string} for {double} for apartment {int} is marked as canceled")
    public void thePaymentOfTypeForForApartmentIsMarkedAsCanceled(String type, double amount, int aptNumber) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        
        List<Payment> activePayments = paymentRepository.findAllByApartmentIdAndTypeAndBillingYearAndBillingMonthAndIsCancelledFalse(
                testApartment.getId(), PaymentType.valueOf(type), currentYear, currentMonth);
        
        assertTrue(activePayments.isEmpty());
    }
}
