package dev.pheological.hoplite_tweaks.apollo;

/** Keeps Lunar's native Apollo handling authoritative when running inside Lunar Client. */
public final class ApolloCompatibility {
    // Bundled in Lunar's client JAR. Probe a resource without initializing its obfuscated classes.
    private static final String LUNAR_APOLLO_RESOURCE = "assets/lunar/apollo/button_circle.png";

    private ApolloCompatibility() {
    }

    public static boolean shouldRegisterChannel(ClassLoader loader) {
        return loader.getResource(LUNAR_APOLLO_RESOURCE) == null;
    }
}
