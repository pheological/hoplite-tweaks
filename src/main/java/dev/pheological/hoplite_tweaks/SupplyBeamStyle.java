package dev.pheological.hoplite_tweaks;

/** Projection and color math for the supply beam's soft, constant apparent width. */
final class SupplyBeamStyle {
    private SupplyBeamStyle() { }

    static double widthAtDepth(double depth, int thicknessPercent) {
        return Math.max(0, depth) * 0.025D * thicknessPercent / 100.0D;
    }

    static int shade(int color, float opacity, float whiteMix) {
        int alpha = Math.round((color >>> 24) * opacity);
        int red = lighten(color >>> 16 & 255, whiteMix);
        int green = lighten(color >>> 8 & 255, whiteMix);
        int blue = lighten(color & 255, whiteMix);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int lighten(int channel, float whiteMix) {
        return Math.round(channel + (255 - channel) * whiteMix);
    }
}
