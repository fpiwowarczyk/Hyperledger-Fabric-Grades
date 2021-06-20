import application.java.FileHandler;
import application.java.GradeController;
import application.java.Organizations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TimeTests {
    private static GradeController tested;

    @BeforeAll
    static void logAsUser() throws Exception {
        tested = new GradeController();

    }

    @Test
    public void timeOfAdding() throws Exception {
        MockedStatic<FileHandler> fh = mockStatic(FileHandler.class);
        Contract contract;
        byte[] result;
        final ObjectMapper objectMapper = new ObjectMapper();
        Gateway.Builder builder;
        String currentUser = "Prof";
        Organizations currentOrganization = Organizations.ORG1;
        Path networkConfigPath = Paths.get("../", "test-network", "organizations", "peerOrganizations", currentOrganization.name().toLowerCase() + ".example.com", "connection-" + currentOrganization.name().toLowerCase() + ".yaml");
        try {
            Path walletPath = Paths.get("../", currentOrganization.name().toLowerCase() + "Wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);
            builder = Gateway.createBuilder();
            builder.identity(wallet, currentUser).networkConfig(networkConfigPath).discovery(true);
        } catch (Exception e) {
            System.out.println("No such user as " + currentUser);
        }
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

}
