import application.java.GradeController;
import application.java.Organizations;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class TimeTests {
    private static GradeController tested;

    @BeforeAll
    static void logAsUser() throws Exception {
        tested = new GradeController();
    }

    @Test
    public void timeOfAdding() throws IOException {
        tested.logIn(Organizations.ORG1, "admin");
        Instant start = Instant.now();
        tested.addGrade(2.0, "Math", "teacher", "stubent");
        Instant finish = Instant.now();
        System.out.println("Time elapsed: " + Duration.between(start, finish).toMillis());

    }


}
