package devs.group5.rms.stepdefinitions;

import devs.group5.rms.TpGestionApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TpGestionApplication.class)
public class CucumberSpringConfiguration {
}
