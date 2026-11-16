package dev.pheological.hoplite_tweaks.apollo;

/** Keeps an existing mod's or Lunar Client's Apollo handling authoritative. */
public final class ApolloCompatibility {
    // Bundled in Lunar's client JAR. Probe a resource without initializing its obfuscated classes.
    private static final String LUNAR_APOLLO_RESOURCE = "assets/lunar/apollo/button_circle.png";

    private static volatile boolean observeNativeChannel;

    private ApolloCompatibility() {
    }

    public static boolean shouldRegisterChannel(ClassLoader loader, boolean channelAlreadyRegistered) {
        observeNativeChannel = channelAlreadyRegistered || loader.getResource(LUNAR_APOLLO_RESOURCE) != null;
        return !observeNativeChannel;
    }

    public static boolean observeNativeChannel() {
        return observeNativeChannel;
    }
}
