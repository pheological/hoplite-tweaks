package dev.pheological.hoplite_tweaks.apollo;

import dev.pheological.hoplite_tweaks.network.ApolloPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Keeps an existing mod's or Lunar Client's Apollo handling authoritative. */
public final class ApolloCompatibility {
    // Bundled in Lunar's client JAR. Probe a resource without initializing its obfuscated classes.
    private static final String LUNAR_APOLLO_RESOURCE = "assets/lunar/apollo/button_circle.png";

    private static volatile boolean observeNativeChannel;

    private ApolloCompatibility() {
    }

    public static boolean registerChannel(ClassLoader loader,
            PayloadTypeRegistry<RegistryFriendlyByteBuf> registry,
            Runnable installReceiver) {
        boolean registered = ((PayloadTypeRegistryImpl<?>) registry)
            .get(ApolloPayload.TYPE.id()) != null;
        if (!shouldRegisterChannel(loader, registered)) return false;
        registry.register(ApolloPayload.TYPE,
            ApolloPayload.CODEC);
        installReceiver.run();
        return true;
    }

    public static boolean shouldRegisterChannel(ClassLoader loader, boolean channelAlreadyRegistered) {
        observeNativeChannel = channelAlreadyRegistered || loader.getResource(LUNAR_APOLLO_RESOURCE) != null;
        return !observeNativeChannel;
    }

    public static boolean observeNativeChannel() {
        return observeNativeChannel;
    }
}
