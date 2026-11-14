package com.xerion.forge.chams;

import com.xerion.common.visual.ChamsMaterial;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.runtime.SocialProfileStore;
import com.xerion.forge.visual.VisualTargetFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ChamsProfileResolver {
    private ChamsProfileResolver() {
    }

    public static Profile profile(LivingEntity entity) {
        if (entity == null || entity.isSpectator()) {
            return Profile.NONE;
        }

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return Profile.NONE;
        }

        if (entity instanceof Player player) {
            if (player == localPlayer) {
                return visibleProfile(entity, VisualSettings.visualLocalEnabled() ? Profile.LOCAL : Profile.NONE);
            }
            if (VisualSettings.visualTeamEnabled()
                    && (SocialProfileStore.isFriend(player) || SocialProfileStore.isStructuralTeamMate(localPlayer, player))) {
                return visibleProfile(entity, Profile.TEAM);
            }
        }
        Profile profile = VisualSettings.visualEspEnabled() && VisualTargetFilter.isEnemyVisualTarget(localPlayer, entity)
                ? Profile.ENEMY
                : Profile.NONE;
        return visibleProfile(entity, profile);
    }

    private static Profile visibleProfile(LivingEntity entity, Profile profile) {
        if (profile == Profile.NONE || !entity.isInvisible()) {
            return profile;
        }
        return profile.showInvisible() ? profile : Profile.NONE;
    }

    public enum Profile {
        NONE,
        ENEMY,
        TEAM,
        LOCAL;

        public boolean visibleEnabled(LivingEntity entity) {
            return switch (this) {
                case ENEMY -> VisualSettings.visualPlayerChams()
                        || VisualSettings.visualOnHitChams() && entity.hurtTime > 0
                        || VisualSettings.visualRagdollsChams() && entity.deathTime > 0;
                case TEAM -> VisualSettings.visualTeamPlayerChams();
                case LOCAL -> VisualSettings.visualLocalPlayerChams();
                case NONE -> false;
            };
        }

        public ChamsMaterial visibleMaterial(LivingEntity entity) {
            return switch (this) {
                case ENEMY -> {
                    if (VisualSettings.visualRagdollsChams() && entity.deathTime > 0) {
                        yield ChamsMaterial.fromLabel(VisualSettings.visualRagdollsChamsMode());
                    }
                    if (VisualSettings.visualOnHitChams() && entity.hurtTime > 0) {
                        yield ChamsMaterial.fromLabel(VisualSettings.visualOnHitChamsMode());
                    }
                    yield VisualSettings.visualPlayerChams()
                            ? ChamsMaterial.fromLabel(VisualSettings.visualPlayerChamsMode())
                            : null;
                }
                case TEAM -> VisualSettings.visualTeamPlayerChams()
                        ? ChamsMaterial.fromLabel(VisualSettings.visualTeamPlayerChamsMode())
                        : null;
                case LOCAL -> {
                    if (VisualSettings.visualLocalPlayerChams()) {
                        yield ChamsMaterial.fromLabel(VisualSettings.visualLocalPlayerChamsMode());
                    }
                    yield null;
                }
                case NONE -> null;
            };
        }

        public int visibleColor(LivingEntity entity) {
            return switch (this) {
                case ENEMY -> {
                    if (VisualSettings.visualRagdollsChams() && entity.deathTime > 0) {
                        yield VisualSettings.visualRagdollsChamsColor();
                    }
                    if (VisualSettings.visualOnHitChams() && entity.hurtTime > 0) {
                        yield VisualSettings.visualOnHitChamsColor();
                    }
                    yield VisualSettings.visualPlayerChamsColor();
                }
                case TEAM -> VisualSettings.visualTeamPlayerChamsColor();
                case LOCAL -> entity instanceof Player player
                        ? LocalVisualChams.localPlayerColor(player, VisualSettings.visualLocalPlayerChamsColor())
                        : VisualSettings.visualLocalPlayerChamsColor();
                case NONE -> 0;
            };
        }

        public boolean throughWallsEnabled() {
            return switch (this) {
                case ENEMY -> VisualSettings.visualBehindWallsChams();
                case TEAM -> VisualSettings.visualTeamBehindWallsChams();
                case LOCAL, NONE -> false;
            };
        }

        public ChamsMaterial throughWallsMaterial() {
            return switch (this) {
                case ENEMY -> ChamsMaterial.fromLabel(VisualSettings.visualBehindWallsChamsMode());
                case TEAM -> ChamsMaterial.fromLabel(VisualSettings.visualTeamBehindWallsChamsMode());
                case LOCAL, NONE -> null;
            };
        }

        public int throughWallsColor() {
            return switch (this) {
                case ENEMY -> VisualSettings.visualBehindWallsChamsColor();
                case TEAM -> VisualSettings.visualTeamBehindWallsChamsColor();
                case LOCAL, NONE -> 0;
            };
        }

        public boolean glowEnabled() {
            return switch (this) {
                case ENEMY -> VisualSettings.visualGlow();
                case TEAM -> VisualSettings.visualTeamGlow();
                case LOCAL -> VisualSettings.visualLocalGlow();
                case NONE -> false;
            };
        }

        public boolean showInvisible() {
            return switch (this) {
                case ENEMY -> VisualSettings.visualEnemiesShowInvisible();
                case TEAM -> VisualSettings.visualTeamShowInvisible();
                case LOCAL -> VisualSettings.visualLocalShowInvisible();
                case NONE -> false;
            };
        }

        public int glowColor() {
            return switch (this) {
                case ENEMY -> VisualSettings.visualGlowColor();
                case TEAM -> VisualSettings.visualTeamGlowColor();
                case LOCAL -> VisualSettings.visualLocalGlowColor();
                case NONE -> 0;
            };
        }

        public boolean hasAnyChams(LivingEntity entity) {
            return visibleEnabled(entity) || throughWallsEnabled();
        }
    }
}
