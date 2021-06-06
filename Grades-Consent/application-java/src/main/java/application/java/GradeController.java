package application.java;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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
    private Path walletPath;
    private Wallet wallet;
    private Path networkConfigPath;
    Gateway.Builder builder;


    GradeController() throws IOException {
        objectMapper = new ObjectMapper();
    }

    @GetMapping("/logIn")
    public String logIn(@RequestParam Organizations org, @RequestParam String userName) {
        //Current location of connection file for organization
        networkConfigPath = Paths.get("test-network", "organizations", "peerOrganizations", org.name().toLowerCase() + ".example.com", "connection-" + org.name().toLowerCase() + ".yaml");
        try {
            walletPath = Paths.get(org.name().toLowerCase() + "Wallet");
            wallet = Wallets.newFileSystemWallet(walletPath);
            builder = Gateway.createBuilder();
            builder.identity(wallet, userName).networkConfig(networkConfigPath).discovery(true);
        } catch (Exception e) {
            LOGGER.error("No such user as " + userName);
        }
        String returnMsg = "Successfully logged user " + userName;
        System.out.println(returnMsg);
        return returnMsg;
    }

    @GetMapping("/addWallet")
    public String addWallet(@RequestParam Organizations org) throws Exception {
        enrollAdmin(org);
        String returnMsg = "Walled added";
        System.out.println(returnMsg);
        return returnMsg;
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam String userName, @RequestParam Organizations org) throws Exception {
        registerUser(userName, org);
        String returnMsg = "User added: " + userName;
        System.out.println(returnMsg);
        return returnMsg;
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

    @GetMapping("/student")
    public List<Grade> getGradesForStudent(@RequestParam String studentName) throws IOException {
        LOGGER.info("Getting grades for " + studentName);
        try (Gateway gateway = builder.connect()) {
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.evaluateTransaction("getGradesForStudent", studentName);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        // Map Json to Grades object
        return objectMapper.readValue(result, new TypeReference<List<Grade>>() {
        });
    }

    @PostMapping("/grades")
    public Grade addGrade(@RequestParam Double gradeValue,
                          @RequestParam String subject,
                          @RequestParam String teacher,
                          @RequestParam String student) throws IOException {
        try (Gateway gateway = builder.connect()) {
            network = gateway.getNetwork("mychannel");
            contract = network.getContract("grades");
            result = contract.submitTransaction("addGrade", gradeValue.toString(), subject, teacher, student);
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

    private void enrollAdmin(@NotNull Organizations org) throws Exception {
        Properties props = new Properties();
        HFCAClient caClient = null;
        String mspId = "";
        props.put("pemFile", "test-network/organizations/peerOrganizations/" + org.name().toLowerCase() + ".example.com/ca/ca." + org.name().toLowerCase() + ".example.com-cert.pem");
        props.put("allowAllHostNames", "true");

        if (org.equals(Organizations.ORG1)) {
            caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            mspId = "Org1MSP";
        } else if (org.equals(Organizations.ORG2)) {
            caClient = HFCAClient.createNewInstance("https://localhost:8054", props);
            mspId = "Org2MSP";
        }
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get(org.name().toLowerCase() + "Wallet"));

        if (wallet.get("admin") != null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            return;
        }

        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
        Identity user = Identities.newX509Identity(mspId, enrollment);
        wallet.put("admin", user);
        System.out.println("Successfully enrolled user \"admin\" and imported it into the wallet");
    }

    private void registerUser(String userName, Organizations org) throws Exception {
        Properties props = new Properties();
        HFCAClient caClient = null;
        String mspId = "";
        String affiliation = org.name().toLowerCase() + ".department1";
        props.put("pemFile", "test-network/organizations/peerOrganizations/" + org.name().toLowerCase() + ".example.com/ca/ca." + org.name().toLowerCase() + ".example.com-cert.pem");
        props.put("allowAllHostNames", "true");

        if (org.equals(Organizations.ORG1)) {
            caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            mspId = "Org1MSP";
        } else if (org.equals(Organizations.ORG2)) {
            caClient = HFCAClient.createNewInstance("https://localhost:8054", props);
            mspId = "Org2MSP";
        }
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get(org.name().toLowerCase() + "Wallet"));

        if (wallet.get("appUser") != null) {
            System.out.printf("An identity for the user \"%s\" already exists in the wallet", userName);
            return;
        }
        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
        if (adminIdentity == null) {
            System.out.println("You need to create wallet first");
            return;
        }

        User admin = new UserImpl("admin", affiliation, adminIdentity, mspId);

        RegistrationRequest registrationRequest = new RegistrationRequest(userName);
        registrationRequest.setAffiliation(affiliation);
        registrationRequest.setEnrollmentID(userName);
        String enrollmentSecret = caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll(userName, enrollmentSecret);
        Identity user = Identities.newX509Identity(mspId, enrollment);
        wallet.put(userName, user);

        System.out.printf("Successfully enrolled user \"%s\" and imported it into the wallet%n", userName);
    }
}
