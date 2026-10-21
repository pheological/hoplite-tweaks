package dev.pheological.hoplite_tweaks.apollo;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ApolloCompatibilityTest {
    @TempDir Path clientFiles;

    @Test
    void ordinaryFabricRetainsCooldownChannel() throws Exception {
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertTrue(ApolloCompatibility.shouldRegisterChannel(loader));
        }
    }

    @Test
    void lunarClientKeepsOwnershipOfServerRestrictionChannel() throws Exception {
        Path marker = clientFiles.resolve("assets/lunar/apollo/button_circle.png");
        Files.createDirectories(marker.getParent());
        Files.write(marker, new byte[0]);
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertFalse(ApolloCompatibility.shouldRegisterChannel(loader));
        }
    }
}
