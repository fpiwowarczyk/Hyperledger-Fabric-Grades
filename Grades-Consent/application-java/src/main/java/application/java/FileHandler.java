package application.java;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {


    public static File createFileForRoles(String userName, Organizations org) throws IOException {
        File rolesFile = new File("error.txt");
        try {
            rolesFile = new File(org.name().toLowerCase() + "Wallet/" + userName + ".txt");
            if (rolesFile.createNewFile()) {
                System.out.println("File created: " + userName + ".txt");
            } else {
                System.out.println("File already exists");
            }
            return rolesFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rolesFile;
    }

    public static void writeRolesToFile(File rolesFile, Set<String> roles) throws IOException {
        try {
            FileWriter writer = new FileWriter(rolesFile.getPath());
            for (String role : roles) {
                writer.write(role + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readRolesFromFile(String username, Organizations org) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get("../",org.name().toLowerCase() + "Wallet/" + username + ".txt"))) {
            return lines.collect(Collectors.joining(";"));
        }
    }
}
