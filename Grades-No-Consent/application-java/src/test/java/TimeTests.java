import application.java.GradeController;
import application.java.Organizations;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.hyperledger.fabric.gateway.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimeTests {
    private static GradeController tested;

    @BeforeAll
    static void logAsUser() throws Exception {
        tested = new GradeController();
    }

    @Test
    public void timeOfAdding() throws Exception {
        tested.logIn(Organizations.ORG1, "admin");
        List<Long> listOfTimesWriting = new ArrayList<>();
        for (int i = 0; i <= 99; i++) {
            Instant start = Instant.now();
            tested.addGrade(2.0, "Math", "teacher", "student");
            Instant finish = Instant.now();
            Long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("Time elapsed: " + timeElapsed);
            listOfTimesWriting.add(timeElapsed);
        }


        List<Long> listOfTimesReading = new ArrayList<>();
        for (int i = 0; i <= 99; i++) {
            Instant start = Instant.now();
            tested.getGrade("student" + i);
            Instant finish = Instant.now();
            Long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("Time elapsed: " + timeElapsed);
            listOfTimesReading.add(timeElapsed);
        }
        System.out.println(listOfTimesWriting);
        System.out.println(listOfTimesReading);
    }

    @Test
    public void timeOfWritingBunch() throws Exception {
        Network network;
        Contract contract;
        byte[] result;
        final ObjectMapper objectMapper;
        Gateway.Builder builder = null;
        String userName = "admin";
        Organizations org = Organizations.ORG1;
        Path networkConfigPath = Paths.get("../", "test-network", "organizations", "peerOrganizations", org.name().toLowerCase() + ".example.com", "connection-" + org.name().toLowerCase() + ".yaml");
        try {
            Path walletPath = Paths.get(org.name().toLowerCase() + "Wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);
            builder = Gateway.createBuilder();
            builder.identity(wallet, userName).networkConfig(networkConfigPath).discovery(true);
            String returnMsg = "Successfully logged user " + userName;
            System.out.println(returnMsg);
        } catch (Exception e) {
            String returnMsg = "No such user as " + userName;
            System.out.println(returnMsg);
        }
        Double gradeValue = 2.0;
        String subject = "test";
        String teacher = "TestT";
        String student = "Test";

        List<Long> listOfTimesWriting = new ArrayList<>();
        List<Long> listOfTimesReading = new ArrayList<>();
        try (Gateway gateway = builder.connect()) {
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            for (int i = 0; i <= 99; i++) {
                Instant start = Instant.now();
                result = contract.submitTransaction("addGrade", gradeValue.toString(), subject, teacher, student);
                Instant finish = Instant.now();
                Long timeElapsed = Duration.between(start, finish).toMillis();
                listOfTimesWriting.add(timeElapsed);
            }

        } catch (Exception e) {
            System.err.println(e);
        }

        try (Gateway gateway = builder.connect()) {
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            for (int i = 0; i <= 99; i++) {
                Instant start = Instant.now();
                result = contract.submitTransaction("ReadGrade", "Test" + i);
                Instant finish = Instant.now();
                Long timeElapsed = Duration.between(start, finish).toMillis();
                listOfTimesReading.add(timeElapsed);
            }

        } catch (Exception e) {
            System.err.println(e);
        }
        System.out.println(listOfTimesWriting);
        System.out.println(listOfTimesReading);
    }


}
