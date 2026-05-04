package SourceCode;

import org.update4j.Configuration;
import org.update4j.UpdateOptions;
import org.update4j.Archive;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Launcher {

    private static final String GITHUB_USER = "bajanandrei007-source";
    private static final String GITHUB_REPO = "java-gui-app";

    private static final String CONFIG_URL =
        "https://raw.githubusercontent.com/" + GITHUB_USER + "/" + GITHUB_REPO + "/main/version.xml";

    private static final Path LOCAL_DIR = Paths.get(System.getProperty("user.home"), ".logiclab");
    private static final Path LOCAL_JAR = LOCAL_DIR.resolve("LogicLab.jar");
    private static final Path LOCAL_CONFIG = LOCAL_DIR.resolve("version.xml");
    
    // Using a ZIP archive and a pending config file instead of an update folder
    private static final Path UPDATE_ZIP = LOCAL_DIR.resolve("update.zip");
    private static final Path PENDING_CONFIG = LOCAL_DIR.resolve("version.pending.xml");

    public static void main(String[] args) {
        // 1. Apply any pending updates (Must do this BEFORE loading any JARs to avoid Windows file locks)
        if (Files.exists(UPDATE_ZIP)) {
            try {
                Files.createDirectories(LOCAL_DIR);
                
                // Uses update4j's native installer to safely extract the archive
                Archive.read(UPDATE_ZIP).install();
                Files.deleteIfExists(UPDATE_ZIP); // Clean up the archive after install
                
                // Update the local config so the application knows it is on the new version
                if (Files.exists(PENDING_CONFIG)) {
                    Files.move(PENDING_CONFIG, LOCAL_CONFIG, StandardCopyOption.REPLACE_EXISTING);
                }
                System.out.println("Applied pending update successfully from ZIP.");
            } catch (Exception e) {
                System.err.println("Failed to apply pending update: " + e.getMessage());
            }
        }

        // 2. Launch the actual application (uses downloaded update if available)
        launchApp();

        // 3. Check for future updates in the background
        new Thread(() -> {
            try {
                URL configUrl = new URL(CONFIG_URL);
                try (InputStream in = configUrl.openStream();
                     InputStreamReader reader = new InputStreamReader(in)) {
                    
                    Configuration remoteConfig = Configuration.read(reader);

                    System.out.println("Remote timestamp : " + remoteConfig.getTimestamp());
                    System.out.println("Remote base URI  : " + remoteConfig.getBaseUri());
                    remoteConfig.getFiles().forEach(f -> {
                        System.out.println("  File path : " + f.getPath());
                        System.out.println("  File URI  : " + f.getUri());
                        System.out.println("  Size      : " + f.getSize());
                        System.out.println("  Checksum  : " + f.getChecksum());
                    });
                    
                    // Check local version.xml to see if we already have this version
                    Configuration localConfig = null;
                    if (Files.exists(LOCAL_CONFIG)) {
                        try (InputStreamReader localReader = new InputStreamReader(Files.newInputStream(LOCAL_CONFIG))) {
                            localConfig = Configuration.read(localReader);
                        }
                    }

                    // Compare timestamps. If local is missing or older, we need an update!
                    if (localConfig == null || !remoteConfig.getTimestamp().equals(localConfig.getTimestamp())) {
                        System.out.println("Update found! Downloading ZIP in background...");
                        Files.createDirectories(LOCAL_DIR);
                        
                        // Download update into a single zip archive
                        remoteConfig.update(UpdateOptions.archive(UPDATE_ZIP));
                        
                        // Save the new version.xml as a pending file to be applied on next launch
                        try (java.io.Writer writer = Files.newBufferedWriter(PENDING_CONFIG)) {
                            remoteConfig.write(writer);
                        }
                        
                        System.out.println("Update downloaded to ZIP. Will seamlessly apply on next launch.");
                    } else {
                        System.out.println("You are already on the latest version.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Update check failed: " + e.getMessage());
            }
        }, "update-checker").start();
    }

    private static void launchApp() {
        try {
            // If an updated JAR is available, launch it using a custom ClassLoader
            if (Files.exists(LOCAL_JAR)) {
                System.out.println("Bootstrapping UPDATED version from " + LOCAL_JAR);
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{ LOCAL_JAR.toUri().toURL() },
                        ClassLoader.getPlatformClassLoader() // Isolates from the bundled outdated classes
                );
                
                Class<?> mainClass = loader.loadClass("SourceCode.Main");
                SwingUtilities.invokeLater(() -> {
                    try {
                        mainClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return; // Successfully launched update!
            }
        } catch (Exception e) {
            System.err.println("Failed to launch updated version, falling back to bundled: " + e.getMessage());
        }

        // Fallback: Run the original bundled application from the .exe
        System.out.println("Bootstrapping BUNDLED version.");
        SwingUtilities.invokeLater(Main::new);
    }
}