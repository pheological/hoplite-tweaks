package dev.pheological.hoplite_tweaks;

/** Temporary settings captured once per activation, never persisted as preferences. */
final class QuakeProState {
    record Settings(int fov, boolean bobbing) {}
    private Settings original;
    private boolean overrideBobbing;

    boolean active() { return original != null; }

    Settings toggle(Settings current, int maximumFov, boolean autoViewBobbing) {
        if (active()) return restore(current);
        original = current;
        overrideBobbing = autoViewBobbing;
        return new Settings(maximumFov, autoViewBobbing || current.bobbing());
    }

    Settings preferences(Settings current) {
        return active() ? new Settings(original.fov(),
            overrideBobbing ? original.bobbing() : current.bobbing()) : current;
    }

    Settings restore(Settings current) {
        Settings restored = preferences(current);
        original = null;
        overrideBobbing = false;
        return restored;
    }
}
