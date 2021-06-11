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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@RestController
public class GradeController {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    private static final Logger LOGGER = LogManager.getLogger(GradeController.class);
    private Contract contract;
    private byte[] result;
    private final ObjectMapper objectMapper = new ObjectMapper();
    Gateway.Builder builder;
    private String currentUser;
    private Organizations currentOrganization;

    @GetMapping("/grades")
    public List<Grade> getAllGrades() throws IOException {
        LOGGER.info("Getting all grades");
        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            connectToChain(gateway);
            result = contract.evaluateTransaction("getAllGrades", currentUser, roles);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return objectMapper.readValue(result, new TypeReference<List<Grade>>() {
        });
    }

    @GetMapping("/grades/{gradeId}")
    public Grade getGrade(@PathVariable String gradeId) throws IOException {
        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Get grade with id: " + gradeId);
            connectToChain(gateway);
            result = contract.evaluateTransaction("ReadGrade", currentUser, roles, gradeId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return objectMapper.readValue(result, Grade.class);
    }

    @GetMapping("/student")
    public List<Grade> getGradesForStudent(@RequestParam String studentName) throws IOException {
        LOGGER.info("Getting grades for " + studentName);
        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            connectToChain(gateway);
            result = contract.evaluateTransaction("getGradesForStudent", currentUser, roles, studentName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
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
        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            connectToChain(gateway);
            result = contract.submitTransaction("addGrade", currentUser, roles, gradeValue.toString(), subject, teacher, student);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

        return objectMapper.readValue(result, Grade.class);
    }

    @PutMapping("/grades/{gradeId}")
    public Grade updateGrade(@PathVariable String gradeId,
                             @RequestParam Double gradeValue,
                             @RequestParam String subject,
                             @RequestParam String teacher,
                             @RequestParam String student) throws IOException {

        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Update grade with: " + gradeId);
            connectToChain(gateway);
            result = contract.submitTransaction("UpdateGrade", currentUser, roles, gradeId, gradeValue.toString(), subject, teacher, student);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

        return objectMapper.readValue(result, Grade.class);
    }


    @DeleteMapping("/grades/{gradeId}")
    public Grade deleteGrade(@PathVariable String gradeId) throws IOException {
        String roles = FileHandler.readRolesFromFile(currentUser, currentOrganization);
        try (Gateway gateway = builder.connect()) {
            LOGGER.info("Delete grade with id: " + gradeId);
            connectToChain(gateway);
            result = contract.evaluateTransaction("ReadGrade", gradeId);
            contract.submitTransaction("DeleteGrade", currentUser, roles, gradeId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return objectMapper.readValue(result, Grade.class);
    }

    private void connectToChain(Gateway gateway) {
        Network network = gateway.getNetwork("mychannel");
        contract = network.getContract("grades");
    }

    @RestController
    private class LoginController {
        @GetMapping("/logIn")
        public String logIn(@RequestParam Organizations org, @RequestParam String userName) {
            //Current location of connection file for organization
            Path networkConfigPath = Paths.get("test-network", "organizations", "peerOrganizations", org.name().toLowerCase() + ".example.com", "connection-" + org.name().toLowerCase() + ".yaml");
            try {
                Path walletPath = Paths.get(org.name().toLowerCase() + "Wallet");
                Wallet wallet = Wallets.newFileSystemWallet(walletPath);
                builder = Gateway.createBuilder();
                builder.identity(wallet, userName).networkConfig(networkConfigPath).discovery(true);
            } catch (Exception e) {
                LOGGER.error("No such user as " + userName);
            }
            setCurrentUserAndOrganization(userName, org);
            String returnMsg = "Successfully logged user " + userName;
            System.out.println(returnMsg);
            return returnMsg;
        }

        @PostMapping("/addWallet")
        public String addWallet(@RequestParam Organizations org) throws Exception {
            enrollAdmin(org);
            String returnMsg = "Walled added";
            System.out.println(returnMsg);
            return returnMsg;
        }

        @PostMapping("/addUser")
        public String addUser(@RequestParam String userName, @RequestParam Organizations org, @RequestParam Set<String> roles) throws Exception {
            registerUser(userName, org, roles);
            String returnMsg = "User added: " + userName;
            System.out.println(returnMsg);
            return returnMsg;
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
            CryptoSuite cryptoSuite = createCryptoSuite(caClient);
            caClient.setCryptoSuite(cryptoSuite);

            Wallet wallet = Wallets.newFileSystemWallet(Paths.get(org.name().toLowerCase() + "Wallet"));

            if (isAdminPresentInWallet(wallet)) {
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

        private void registerUser(String userName, Organizations org, Set<String> roles) throws Exception {
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

            CryptoSuite cryptoSuite = createCryptoSuite(caClient);
            caClient.setCryptoSuite(cryptoSuite);
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get(org.name().toLowerCase() + "Wallet"));

            if (wallet.get(userName) != null) {
                System.out.printf("An identity for the user \"%s\" already exists in the wallet", userName);
                return;
            }
            X509Identity adminIdentity = (X509Identity) wallet.get("admin");
            if (adminIdentity == null) {
                System.out.println("You need to create wallet first");
                return;
            }

            writeRolesFile(userName, org, roles);
            registerUserInChain(wallet, affiliation, adminIdentity, mspId, userName, caClient);
            System.out.printf("Successfully enrolled user \"%s\" and imported it into the wallet%n", userName);
        }
    }

    private void registerUserInChain(Wallet wallet,
                                     String affiliation,
                                     X509Identity adminIdentity,
                                     String mspId,
                                     String userName,
                                     HFCAClient caClient) throws Exception {
        User admin = new UserImpl("admin", Set.of("Admin"), affiliation, adminIdentity, mspId);
        RegistrationRequest registrationRequest = new RegistrationRequest(userName);
        registrationRequest.setAffiliation(affiliation);
        registrationRequest.setEnrollmentID(userName);
        String enrollmentSecret = caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll(userName, enrollmentSecret);
        Identity user = Identities.newX509Identity(mspId, enrollment);
        wallet.put(userName, user);
    }

    private void writeRolesFile(String userName, Organizations org, Set<String> roles) throws IOException {
        File rolesFile = FileHandler.createFileForRoles(userName, org);
        FileHandler.writeRolesToFile(rolesFile, roles);
    }

    private CryptoSuite createCryptoSuite(HFCAClient caClient) throws Exception {
        return CryptoSuiteFactory.getDefault().getCryptoSuite();
    }

    private boolean isAdminPresentInWallet(Wallet wallet) throws IOException {
        return wallet.get("admin") != null;
    }

    private void setCurrentUserAndOrganization(String userName, Organizations org) {
        currentUser = userName;
        currentOrganization = org;
    }


}
