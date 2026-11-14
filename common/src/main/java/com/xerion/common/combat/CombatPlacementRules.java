package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CombatPlacementRules {
    private static final int AUTO_CRYSTAL_SCAN_HORIZONTAL = 4;
    private static final int AUTO_CRYSTAL_SCAN_DOWN = 3;
    private static final int AUTO_CRYSTAL_SCAN_UP = 2;
    private static final ScanOffset[] AUTO_CRYSTAL_PLACEMENT_OFFSETS = buildCenteredOffsets(
            AUTO_CRYSTAL_SCAN_HORIZONTAL,
            AUTO_CRYSTAL_SCAN_DOWN,
            AUTO_CRYSTAL_SCAN_UP
    );
    private static final ScanOffset[] CARDINAL_OFFSETS = {
            new ScanOffset(0, 0, -1),
            new ScanOffset(0, 0, 1),
            new ScanOffset(-1, 0, 0),
            new ScanOffset(1, 0, 0)
    };

    private CombatPlacementRules() {
    }

    public static int scanRadius(double range) {
        return (int) Math.ceil(range);
    }

    public static double holeFillScore(boolean hasTarget, boolean selfMode,
                                       double targetDistanceSq, double playerDistanceSq) {
        if (hasTarget && !selfMode) {
            return targetDistanceSq * 1.35D + playerDistanceSq * 0.05D;
        }
        return playerDistanceSq;
    }

    public static ScanOffset[] autoCrystalPlacementOffsets() {
        return AUTO_CRYSTAL_PLACEMENT_OFFSETS.clone();
    }

    public static ScanOffset[] webAuraPlacementOffsets(String mode) {
        List<ScanOffset> offsets = new ArrayList<>();
        if (!CombatSettings.WEB_AURA_MODE_HEAD.equals(mode)) {
            offsets.add(new ScanOffset(0, 0, 0));
        }
        if (!CombatSettings.WEB_AURA_MODE_FEET.equals(mode)) {
            offsets.add(new ScanOffset(0, 1, 0));
        }
        return offsets.toArray(ScanOffset[]::new);
    }

    public static ScanOffset[] autoTrapPlacementOffsets(String mode) {
        List<ScanOffset> offsets = new ArrayList<>();
        if (!CombatSettings.AUTO_TRAP_MODE_HEAD.equals(mode)) {
            addCardinalOffsets(offsets, 0);
        }
        if (!CombatSettings.AUTO_TRAP_MODE_FEET.equals(mode)) {
            addCardinalOffsets(offsets, 1);
            offsets.add(new ScanOffset(0, 2, 0));
        }
        return offsets.toArray(ScanOffset[]::new);
    }

    public static ScanOffset[] surroundPlacementOffsets(boolean includeFloor, String mode) {
        List<ScanOffset> offsets = new ArrayList<>();
        if (includeFloor) {
            offsets.add(new ScanOffset(0, -1, 0));
        }
        addCardinalOffsets(offsets, 0);
        if (CombatSettings.SURROUND_MODE_FULL.equals(mode)) {
            addCardinalOffsets(offsets, 1);
        }
        return offsets.toArray(ScanOffset[]::new);
    }

    public static int scanOffsetDistanceSq(int dx, int dy, int dz) {
        return dx * dx + dy * dy + dz * dz;
    }

    private static void addCardinalOffsets(List<ScanOffset> offsets, int y) {
        for (ScanOffset offset : CARDINAL_OFFSETS) {
            offsets.add(new ScanOffset(offset.dx(), y, offset.dz()));
        }
    }

    private static ScanOffset[] buildCenteredOffsets(int horizontalRadius, int downRadius, int upRadius) {
        List<ScanOffset> offsets = new ArrayList<>();
        for (int dy = -downRadius; dy <= upRadius; dy++) {
            for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
                for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                    offsets.add(new ScanOffset(dx, dy, dz));
                }
            }
        }
        offsets.sort(Comparator.comparingInt(offset -> scanOffsetDistanceSq(offset.dx(), offset.dy(), offset.dz())));
        return offsets.toArray(ScanOffset[]::new);
    }

    public record ScanOffset(int dx, int dy, int dz) {
    }
}
