package application.java;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.gateway.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class GradeController {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    private static Logger LOGGER = LogManager.getLogger(GradeController.class);
    private Network network;
    private Contract contract;
    private byte[] result;
    private final ObjectMapper objectMapper;
    private final Path walletPath;
    private final Wallet wallet;
    private final Path networkConfigPath;
    Gateway.Builder builder;


    GradeController() throws IOException {
        walletPath = Paths.get("wallet");
        wallet = Wallets.newFileSystemWallet(walletPath);

        //Current location of connection file for organization
        networkConfigPath = Paths.get("test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.yaml");

        builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser1").networkConfig(networkConfigPath).discovery(true);
        objectMapper = new ObjectMapper();

    }

    @GetMapping("/grades")
    public List<Grade> getAllGrades() throws IOException {
        LOGGER.info("Getting all classes");
        try (Gateway gateway = builder.connect()) {
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.evaluateTransaction("getAllGrades");
        } catch (Exception e) {
            LOGGER.error(e);
        }
        // Map Json to Grades object
        return objectMapper.readValue(result, new TypeReference<List<Grade>>() {
        });
    }

    @GetMapping("/grades/{gradeId}")
    public Grade getGrade(@PathVariable String gradeId) throws IOException {
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Get grade with id: " + gradeId);
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.evaluateTransaction("ReadGrade", gradeId);
        } catch (Exception e) {
            System.err.println(e);
        }
        return objectMapper.readValue(result, Grade.class);
    }

    @PostMapping("/grades")
    public Grade addGrade(@RequestParam String gradeId,
                          @RequestParam Double gradeValue,
                          @RequestParam String subject,
                          @RequestParam String teacher,
                          @RequestParam String student) throws IOException {
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Add grade with: " + gradeId);
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.submitTransaction("addGrade", gradeId, gradeValue.toString(), subject, teacher, student);
        } catch (Exception e) {
            System.err.println(e);
        }

        return objectMapper.readValue(result, Grade.class);
    }

    @PutMapping("/grades/{gradeId}")
    public Grade updateGrade(@PathVariable String gradeId,
                             @RequestParam Double gradeValue,
                             @RequestParam String subject,
                             @RequestParam String teacher,
                             @RequestParam String student) throws IOException {
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Update grade with: " + gradeId);
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.submitTransaction("UpdateGrade", gradeId, gradeValue.toString(), subject, teacher, student);
        } catch (Exception e) {
            System.err.println(e);
        }

        return objectMapper.readValue(result, Grade.class);
    }


    @DeleteMapping("/grades/{gradeId}")
    public Grade deleteGrade(@PathVariable String gradeId) throws IOException {
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Delete grade with id: " + gradeId);
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.evaluateTransaction("ReadGrade", gradeId);
            contract.submitTransaction("DeleteGrade", gradeId);
        } catch (Exception e) {
            System.err.println(e);
        }
        return objectMapper.readValue(result, Grade.class);

    }
}
