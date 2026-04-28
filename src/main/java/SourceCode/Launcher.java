package SourceCode;

import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.UpdateOptions;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Launcher {

    // ── Replace with your actual GitHub username and repo name ──
    private static final String GITHUB_USER = "your-github-username";
    private static final String GITHUB_REPO = "java-gui-app";

    private static final String CONFIG_URL =
        "https://raw.githubusercontent.com/" + GITHUB_USER + "/" + GITHUB_REPO + "/main/version.xml";

    private static final Path LOCAL_JAR =
        Paths.get(System.getProperty("user.home"), ".logiclab", "LogicLab.jar");

    public static void main(String[] args) {
        try {
            // Show a simple loading message
            System.out.println("LogicLab Launcher — checking for updates...");

            // Fetch the remote version config
            URL configUrl = new URL(CONFIG_URL);
            try (InputStream in = configUrl.openStream();
                 InputStreamReader reader = new InputStreamReader(in)) {

                Configuration config = Configuration.read(reader);

                // Create install directory if it doesn't exist
                Files.createDirectories(LOCAL_JAR.getParent());

                // Check and apply updates if needed
                if (config.requiresUpdate()) {
                    System.out.println("Update found! Downloading...");
                    config.update(UpdateOptions.archive(LOCAL_JAR.getParent()));
                    System.out.println("Update complete. Relaunching...");
                } else {
                    System.out.println("Already up to date.");
                }
            }

            // Launch the actual app
            launchApp();

        } catch (Exception e) {
            // If update check fails (no internet, etc.), still launch the app
            System.err.println("Update check failed: " + e.getMessage());
            System.out.println("Launching app without update check...");
            launchApp();
        }
    }

    private static void launchApp() {
        SwingUtilities.invokeLater(Main::new);
    }
}