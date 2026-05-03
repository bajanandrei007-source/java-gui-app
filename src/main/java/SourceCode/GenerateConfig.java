package SourceCode;

import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Run this ONCE after building LogicLab.jar to regenerate version.xml.
 * It auto-calculates the correct CRC32 checksum and size from the actual JAR file.
 *
 * Usage:
 *   java -cp target/LogicLab.jar SourceCode.GenerateConfig <path-to-jar>
 *
 * Example:
 *   java -cp target/LogicLab.jar SourceCode.GenerateConfig target/LogicLab.jar
 *
 * Then commit and push the generated version.xml to GitHub.
 */
public class GenerateConfig {

    private static final String BASE_URI    = "https://github.com/bajanandrei007-source/java-gui-app/releases/latest/download/";
    private static final String REMOTE_URI  = "LogicLab.jar";
    private static final String LOCAL_PATH  = "${user.home}/.logiclab/LogicLab.jar";
    private static final String OUTPUT_FILE = "version.xml";

    public static void main(String[] args) {
        String jarPath = args.length > 0
            ? args[0]
            : "C:\\Program Files\\LogicLab\\app\\LogicLab.jar"; // fallback for manual runs

        try {
            System.out.println("Reading JAR from: " + jarPath);

            Configuration config = Configuration.builder()
                .baseUri(BASE_URI)
                .file(FileMetadata.readFrom(jarPath)
                    .path(LOCAL_PATH)
                    .uri(REMOTE_URI))
                .build();

            try (Writer out = Files.newBufferedWriter(Paths.get(OUTPUT_FILE))) {
                config.write(out);
            }

            System.out.println("version.xml generated successfully.");
            System.out.println("Commit and push it to GitHub before your next release.");

        } catch (Exception e) {
            System.err.println("Failed to generate version.xml: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}