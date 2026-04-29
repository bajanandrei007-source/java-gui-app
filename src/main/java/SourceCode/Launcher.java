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
    private static final String GITHUB_USER = "bajanandrei007-source";
    private static final String GITHUB_REPO = "java-gui-app";

    private static final String CONFIG_URL =
        "https://raw.githubusercontent.com/" + GITHUB_USER + "/" + GITHUB_REPO + "/main/version.xml";

    private static final Path LOCAL_JAR =
        Paths.get(System.getProperty("user.home"), ".logiclab", "LogicLab.jar");

    public static void main(String[] args) {
            // Launch the actual app
            launchApp();

        new Thread(() -> {
            try {
                URL configUrl = new URL(CONFIG_URL);
                try (InputStream in = configUrl.openStream();
                    InputStreamReader reader = new InputStreamReader(in)) {
                    Configuration config = Configuration.read(reader);
                    Files.createDirectories(LOCAL_JAR.getParent());
                    if (config.requiresUpdate()) {
                        System.out.println("Update found! Downloading in background...");
                        config.update(UpdateOptions.archive(LOCAL_JAR.getParent()));
                        System.out.println("Update downloaded. Will apply on next launch.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Update check failed: " + e.getMessage());
            }
        }, "update-checker").start();
    }

    private static void launchApp() {
        SwingUtilities.invokeLater(Main::new);
    }
}