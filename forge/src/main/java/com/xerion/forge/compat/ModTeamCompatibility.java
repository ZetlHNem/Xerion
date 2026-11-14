package com.xerion.forge.compat;

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

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ModTeamCompatibility {
    private static final String[] ROOT_CLASSES = {
            "dev.ftb.mods.ftbteams.api.FTBTeamsAPI",
            "dev.ftb.mods.ftbteams.FTBTeamsAPI",
            "dev.ftb.mods.ftbteams.FTBTeams",
            "dev.ftb.mods.ftbteams.data.TeamManager",
            "dev.ftb.mods.ftbteams.client.ClientTeamManager",
            "xaero.pac.OpenPartiesAndClaims",
            "xaero.pac.common.server.api.OpenPACServerAPI",
            "xaero.pac.common.parties.party.PartyManager",
            "xaero.pac.common.server.parties.party.PartyManager",
            "earth.terrarium.argonauts.api.ArgonautsApi",
            "earth.terrarium.argonauts.api.client.ArgonautsClientApi",
            "earth.terrarium.argonauts.common.handlers.TeamHandler",
            "earth.terrarium.argonauts.common.team.TeamManager"
    };

    private static final String[] ROOT_METHODS = {
            "api", "get", "getInstance", "instance", "getManager", "getTeamManager",
            "getClientManager", "getPartyManager", "manager"
    };

    private static final String[] ROOT_FIELDS = {
            "INSTANCE", "API", "MANAGER", "TEAM_MANAGER", "PARTY_MANAGER"
    };

    private static final String[] MANAGER_METHODS = {
            "getManager", "getTeamManager", "getClientManager", "getPartyManager",
            "getPartyManager", "teams", "teamManager", "partyManager", "manager"
    };

    private static final String[] SAME_TEAM_METHODS = {
            "arePlayersInSameTeam", "arePlayersInSameParty", "arePlayersAllied",
            "areTeammates", "areTeamMates", "sameTeam", "sameParty",
            "isTeamMate", "isTeammate", "isAlly", "isAllied"
    };

    private static final String[] TEAM_LOOKUP_METHODS = {
            "getPlayerTeam", "getTeamForPlayer", "getTeamForPlayerID", "getTeamForPlayerId",
            "getTeamByPlayer", "getTeamByMember", "getPartyForPlayer", "getPlayerParty",
            "getPartyByPlayer", "getPartyByMember", "getParty", "getTeam"
    };

    private static final String[] ID_METHODS = {
            "getId", "getID", "id", "getTeamId", "getTeamID", "teamId", "getPartyId",
            "getPartyID", "partyId", "getName", "name"
    };

    private ModTeamCompatibility() {
    }

    public static boolean sameModTeam(LocalPlayer localPlayer, Player other) {
        if (localPlayer == null || other == null || other == localPlayer || FakePlayerRegistry.isDummy(other)) {
            return false;
        }
        UUID localId = localPlayer.getUUID();
        UUID otherId = other.getUUID();
        String localName = localPlayer.getGameProfile().getName();
        String otherName = other.getGameProfile().getName();

        for (String className : ROOT_CLASSES) {
            Class<?> type = load(className);
            if (type == null) {
                continue;
            }
            if (sameByObject(type, null, localId, otherId, localName, otherName, localPlayer, other)) {
                return true;
            }
            for (Object root : roots(type)) {
                if (sameByObject(root.getClass(), root, localId, otherId, localName, otherName, localPlayer, other)) {
                    return true;
                }
                for (Object teamHandle : teamHandles(root)) {
                    if (sameByObject(teamHandle.getClass(), teamHandle, localId, otherId, localName, otherName, localPlayer, other)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean sameByObject(Class<?> type, Object target, UUID localId, UUID otherId,
                                        String localName, String otherName, LocalPlayer localPlayer, Player other) {
        Boolean direct = directSameTeam(type, target, localId, otherId, localName, otherName, localPlayer, other);
        if (direct != null) {
            return direct;
        }

        Object localTeam = findTeam(type, target, localId, localName, localPlayer);
        Object otherTeam = findTeam(type, target, otherId, otherName, other);
        if (localTeam == null || otherTeam == null) {
            return false;
        }
        if (localTeam == otherTeam) {
            return true;
        }
        Object localKey = teamKey(localTeam);
        Object otherKey = teamKey(otherTeam);
        return localKey != null && Objects.equals(localKey, otherKey);
    }

    private static Boolean directSameTeam(Class<?> type, Object target, UUID localId, UUID otherId,
                                          String localName, String otherName, LocalPlayer localPlayer, Player other) {
        for (String name : SAME_TEAM_METHODS) {
            Boolean result = invokeBooleanPair(type, target, name, localId, otherId);
            if (result != null) {
                return result;
            }
            result = invokeBooleanPair(type, target, name, localName, otherName);
            if (result != null) {
                return result;
            }
            result = invokeBooleanPair(type, target, name, localPlayer, other);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static Object findTeam(Class<?> type, Object target, UUID uuid, String name, Player player) {
        for (String methodName : TEAM_LOOKUP_METHODS) {
            Object team = invokeLookup(type, target, methodName, uuid);
            if (team != null) {
                return team;
            }
            team = invokeLookup(type, target, methodName, name);
            if (team != null) {
                return team;
            }
            team = invokeLookup(type, target, methodName, player);
            if (team != null) {
                return team;
            }
        }
        return null;
    }

    private static Object teamKey(Object team) {
        Object value = unwrap(team);
        if (value == null) {
            return null;
        }
        for (String methodName : ID_METHODS) {
            Object key = invokeNoArg(value.getClass(), value, methodName);
            if (key != null) {
                return key;
            }
        }
        return value;
    }

    private static Object[] roots(Class<?> type) {
        Object[] roots = new Object[ROOT_METHODS.length + ROOT_FIELDS.length];
        int count = 0;
        for (String methodName : ROOT_METHODS) {
            Object root = invokeNoArg(type, null, methodName);
            if (root != null && root != type) {
                roots[count++] = root;
            }
        }
        for (String fieldName : ROOT_FIELDS) {
            Object root = readStaticField(type, fieldName);
            if (root != null) {
                roots[count++] = root;
            }
        }
        Object[] compact = new Object[count];
        System.arraycopy(roots, 0, compact, 0, count);
        return compact;
    }

    private static Object[] teamHandles(Object root) {
        Object[] teamHandles = new Object[MANAGER_METHODS.length];
        int count = 0;
        for (String methodName : MANAGER_METHODS) {
            Object teamHandle = invokeNoArg(root.getClass(), root, methodName);
            if (teamHandle != null && teamHandle != root) {
                teamHandles[count++] = teamHandle;
            }
        }
        Object[] compact = new Object[count];
        System.arraycopy(teamHandles, 0, compact, 0, count);
        return compact;
    }

    private static Boolean invokeBooleanPair(Class<?> type, Object target, String methodName, Object first, Object second) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 2 || method.getReturnType() != boolean.class) {
                continue;
            }
            if (!canInvoke(target, method) || !compatible(method.getParameterTypes()[0], first) || !compatible(method.getParameterTypes()[1], second)) {
                continue;
            }
            try {
                method.setAccessible(true);
                return (Boolean) method.invoke(target, first, second);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static Object invokeLookup(Class<?> type, Object target, String methodName, Object argument) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1 || !canInvoke(target, method)) {
                continue;
            }
            if (!compatible(method.getParameterTypes()[0], argument)) {
                continue;
            }
            try {
                method.setAccessible(true);
                return unwrap(method.invoke(target, argument));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static Object invokeNoArg(Class<?> type, Object target, String methodName) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 0 || !canInvoke(target, method)) {
                continue;
            }
            try {
                method.setAccessible(true);
                return unwrap(method.invoke(target));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static Object readStaticField(Class<?> type, String fieldName) {
        try {
            Field field = type.getField(fieldName);
            if (!Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            field.setAccessible(true);
            return unwrap(field.get(null));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean canInvoke(Object target, Method method) {
        return target != null || Modifier.isStatic(method.getModifiers());
    }

    private static boolean compatible(Class<?> parameter, Object argument) {
        return argument != null && wrap(parameter).isAssignableFrom(argument.getClass());
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private static Object unwrap(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }
}
