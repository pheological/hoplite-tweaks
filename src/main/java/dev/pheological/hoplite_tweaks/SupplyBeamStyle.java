package dev.pheological.hoplite_tweaks;

import java.util.Locale;

/** Projection and color math for the supply beam's soft, constant apparent width. */
final class SupplyBeamStyle {
    private static final double AIM_TOLERANCE_TANGENT = Math.tan(Math.toRadians(10.0D));

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

    static AimTarget aimTarget(
        double cameraX, double cameraY, double cameraZ,
        double directionX, double directionY, double directionZ,
        double beamX, double beamY, double beamZ,
        double beamHeight, int thicknessPercent
    ) {
        double directionLengthSquared = directionX * directionX
            + directionY * directionY + directionZ * directionZ;
        if (directionLengthSquared == 0.0D || beamHeight < 0.0D) return null;

        double inverseDirectionLength = 1.0D / Math.sqrt(directionLengthSquared);
        double dx = directionX * inverseDirectionLength;
        double dy = directionY * inverseDirectionLength;
        double dz = directionZ * inverseDirectionLength;
        double toBaseX = beamX - cameraX;
        double toBaseY = beamY - cameraY;
        double toBaseZ = beamZ - cameraZ;
        double baseDepth = toBaseX * dx + toBaseY * dy + toBaseZ * dz;
        double baseLengthSquared = toBaseX * toBaseX + toBaseY * toBaseY + toBaseZ * toBaseZ;

        AimTarget best = betterAim(null, candidate(
            toBaseX, toBaseY, toBaseZ, dx, dy, dz, 0.0D
        ));
        best = betterAim(best, candidate(
            toBaseX, toBaseY, toBaseZ, dx, dy, dz, beamHeight
        ));

        // Minimize angular rather than world-space miss distance because the beam's
        // width is proportional to depth and therefore constant on screen.
        double denominator = baseDepth - dy * toBaseY;
        if (Math.abs(denominator) > 1.0E-9D) {
            double closestHeight = (dy * baseLengthSquared - toBaseY * baseDepth) / denominator;
            if (closestHeight > 0.0D && closestHeight < beamHeight) {
                best = betterAim(best, candidate(
                    toBaseX, toBaseY, toBaseZ, dx, dy, dz, closestHeight
                ));
            }
        }

        if (best == null) return null;
        double halfWidth = widthAtDepth(best.rayDistance(), thicknessPercent) * 0.5D;
        double angularTolerance = best.rayDistance() * AIM_TOLERANCE_TANGENT;
        return best.missDistance() <= Math.max(halfWidth, angularTolerance) ? best : null;
    }

    private static AimTarget candidate(
        double toBaseX, double toBaseY, double toBaseZ,
        double dx, double dy, double dz, double heightOffset
    ) {
        double toPointY = toBaseY + heightOffset;
        double rayDistance = toBaseX * dx + toPointY * dy + toBaseZ * dz;
        if (rayDistance <= 0.0D) return null;
        double missX = toBaseX - dx * rayDistance;
        double missY = toPointY - dy * rayDistance;
        double missZ = toBaseZ - dz * rayDistance;
        return new AimTarget(
            heightOffset,
            rayDistance,
            Math.sqrt(missX * missX + missY * missY + missZ * missZ)
        );
    }

    private static AimTarget betterAim(AimTarget current, AimTarget candidate) {
        if (candidate == null) return current;
        if (current == null) return candidate;
        double currentScreenMiss = current.missDistance() / current.rayDistance();
        double candidateScreenMiss = candidate.missDistance() / candidate.rayDistance();
        return candidateScreenMiss < currentScreenMiss ? candidate : current;
    }

    static String distanceLabel(double playerX, double playerZ, double beamX, double beamZ) {
        return String.format(Locale.ROOT, "%.1fm", Math.hypot(beamX - playerX, beamZ - playerZ));
    }

    record AimTarget(double heightOffset, double rayDistance, double missDistance) { }

    private static int lighten(int channel, float whiteMix) {
        return Math.round(channel + (255 - channel) * whiteMix);
    }
}
