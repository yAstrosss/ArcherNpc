package com.yastro.npc.npc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.yastro.npc.display.ItemModels;
import com.yastro.npc.model.Npc;
import com.yastro.npc.model.NpcData;
import com.yastro.npc.skin.SkinData;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PacketNpc {
    private PacketNpc() {}

    private static final int POSE_INDEX = 6;
    private static final int DISPLAY_SCALE_INDEX = 12;
    private static final int DISPLAY_BLOCK_STATE_INDEX = 23;
    private static final int ITEMDISPLAY_ITEM_INDEX = 23;
    private static final int ITEMDISPLAY_TYPE_INDEX = 24;
    private static final byte FLAG_SNEAKING = 0x02;

    public static void spawn(Player viewer, Npc npc) {
        NpcData d = npc.data();
        EntityType type = resolveType(d.tipo());
        boolean isPlayer = type == EntityTypes.PLAYER;
        boolean isDisplay = type == EntityTypes.ITEM_DISPLAY || type == EntityTypes.BLOCK_DISPLAY;
        String name = trim16(d.id());

        if (isPlayer) {
            UserProfile profile = new UserProfile(npc.profileId(), name);
            if (d.skinMirror()) {
                for (var pp : viewer.getPlayerProfile().getProperties()) {
                    if ("textures".equals(pp.getName())) {
                        profile.getTextureProperties().add(new TextureProperty("textures", pp.getValue(), pp.getSignature()));
                        break;
                    }
                }
            } else if (!d.skin().isEmpty()) {
                profile.getTextureProperties().add(new TextureProperty("textures", d.skin().value(), d.skin().signature()));
            }
            var info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, d.showInTab(), 0, GameMode.SURVIVAL, null, null);
            send(viewer, new WrapperPlayServerPlayerInfoUpdate(
                    EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                               WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED), info));
        }

        send(viewer, new WrapperPlayServerSpawnEntity(npc.entityId(), Optional.of(npc.profileId()),
                type, new Vector3d(d.x(), d.y(), d.z()), d.pitch(), d.yaw(), d.yaw(), 0, Optional.empty()));

        send(viewer, new WrapperPlayServerEntityMetadata(npc.entityId(), buildMeta(d, isPlayer, type)));

        if (!isDisplay) send(viewer, new WrapperPlayServerEntityHeadLook(npc.entityId(), d.yaw()));

        String entry = isPlayer ? name : npc.profileId().toString();
        ScoreBoardTeamInfo teamInfo = new ScoreBoardTeamInfo(Component.empty(), Component.empty(), Component.empty(),
                NameTagVisibility.NEVER, d.collidable() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                color(d.glowColor()), OptionData.NONE);
        send(viewer, new WrapperPlayServerTeams(name, TeamMode.CREATE, Optional.of(teamInfo), List.of(entry)));

        if (!isDisplay && d.scale() != 1.0) {
            send(viewer, new WrapperPlayServerUpdateAttributes(npc.entityId(), List.of(
                    new WrapperPlayServerUpdateAttributes.Property(Attributes.SCALE, d.scale(), List.of()))));
        }

        if (isDisplay) {
            double auto = Math.max(0.6, d.scale());
            float w = (float) (d.hitWidth() > 0 ? d.hitWidth() : auto);
            float h = (float) (d.hitHeight() > 0 ? d.hitHeight() : auto);
            send(viewer, new WrapperPlayServerSpawnEntity(npc.interactionId(), Optional.of(UUID.randomUUID()),
                    EntityTypes.INTERACTION,
                    new Vector3d(d.x() + d.hitOffX(), d.y() + d.hitOffY(), d.z() + d.hitOffZ()),
                    0f, 0f, 0f, 0, Optional.empty()));
            List<EntityData<?>> im = new ArrayList<>();
            im.add(new EntityData<>(8, EntityDataTypes.FLOAT, w));
            im.add(new EntityData<>(9, EntityDataTypes.FLOAT, h));
            im.add(new EntityData<>(10, EntityDataTypes.BOOLEAN, true));
            send(viewer, new WrapperPlayServerEntityMetadata(npc.interactionId(), im));
        }

        if (!isDisplay && !d.equipment().isEmpty()) {
            List<Equipment> eq = new ArrayList<>();
            for (var e : d.equipment().entrySet()) {
                EquipmentSlot slot = slot(e.getKey());
                org.bukkit.inventory.ItemStack it = ItemModels.resolve(e.getValue());
                if (slot != null && it != null) eq.add(new Equipment(slot, SpigotConversionUtil.fromBukkitItemStack(it)));
            }
            if (!eq.isEmpty()) send(viewer, new WrapperPlayServerEntityEquipment(npc.entityId(), eq));
        }
    }

    private static List<EntityData<?>> buildMeta(NpcData d, boolean isPlayer, EntityType type) {
        List<EntityData<?>> meta = new ArrayList<>();
        byte flags = 0;
        if (d.glow()) flags |= PlayerMeta.FLAG_GLOWING;
        if ("agachado".equalsIgnoreCase(d.pose())) flags |= FLAG_SNEAKING;
        meta.add(new EntityData<>(PlayerMeta.ENTITY_FLAGS_INDEX, EntityDataTypes.BYTE, flags));

        EntityPose pose = resolvePose(d.pose());
        if (pose != EntityPose.STANDING) meta.add(new EntityData<>(POSE_INDEX, EntityDataTypes.ENTITY_POSE, pose));

        if (isPlayer) {
            meta.add(new EntityData<>(PlayerMeta.SKIN_LAYERS_INDEX, EntityDataTypes.BYTE, PlayerMeta.ALL_LAYERS));
        }
        if (type == EntityTypes.ITEM_DISPLAY || type == EntityTypes.BLOCK_DISPLAY) {
            float s = (float) d.scale();
            meta.add(new EntityData<>(DISPLAY_SCALE_INDEX, EntityDataTypes.VECTOR3F, new Vector3f(s, s, s)));
        }
        if (type == EntityTypes.ITEM_DISPLAY) {
            org.bukkit.inventory.ItemStack bukkit = ItemModels.resolve(d.model());
            if (bukkit != null) {
                meta.add(new EntityData<>(ITEMDISPLAY_ITEM_INDEX, EntityDataTypes.ITEMSTACK,
                        SpigotConversionUtil.fromBukkitItemStack(bukkit)));
            }
            meta.add(new EntityData<>(ITEMDISPLAY_TYPE_INDEX, EntityDataTypes.BYTE, (byte) 8));
        } else if (type == EntityTypes.BLOCK_DISPLAY) {
            int state = blockStateId(d.model());
            if (state >= 0) meta.add(new EntityData<>(DISPLAY_BLOCK_STATE_INDEX, EntityDataTypes.BLOCK_STATE, state));
        }
        return meta;
    }

    private static EntityPose resolvePose(String p) {
        if (p == null) return EntityPose.STANDING;
        return switch (p.toLowerCase()) {
            case "agachado" -> EntityPose.CROUCHING;
            case "dormindo" -> EntityPose.SLEEPING;
            case "nadando" -> EntityPose.SWIMMING;
            case "sentado" -> EntityPose.SITTING;
            default -> EntityPose.STANDING;
        };
    }

    private static EquipmentSlot slot(String key) {
        return switch (key.toLowerCase()) {
            case "capacete" -> EquipmentSlot.HELMET;
            case "peito" -> EquipmentSlot.CHEST_PLATE;
            case "calca", "calça" -> EquipmentSlot.LEGGINGS;
            case "bota" -> EquipmentSlot.BOOTS;
            case "mao", "mão" -> EquipmentSlot.MAIN_HAND;
            case "secundaria", "secundária" -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    private static int blockStateId(String ref) {
        String r = ref == null ? "" : ref;
        int sep = r.indexOf(':');
        String id = sep < 0 ? r : r.substring(sep + 1);
        Material m = Material.matchMaterial(id);
        if (m == null || !m.isBlock()) return -1;
        try {
            return SpigotConversionUtil.fromBukkitBlockData(Bukkit.createBlockData(m)).getGlobalId();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static EntityType resolveType(String tipo) {
        if (tipo == null) return EntityTypes.PLAYER;
        String t = tipo.toLowerCase();
        switch (t) {
            case "player": return EntityTypes.PLAYER;
            case "item": return EntityTypes.ITEM_DISPLAY;
            case "bloco": case "block": return EntityTypes.BLOCK_DISPLAY;

            case "zumbi": return EntityTypes.ZOMBIE;
            case "aldeao": case "aldeão": return EntityTypes.VILLAGER;
            case "esqueleto": return EntityTypes.SKELETON;
            case "golem": return EntityTypes.IRON_GOLEM;
            case "suporte": return EntityTypes.ARMOR_STAND;
            default:
                EntityType et = EntityTypes.getByName(t.contains(":") ? t : "minecraft:" + t);
                return et != null ? et : EntityTypes.PLAYER;
        }
    }

    public static void despawn(Player viewer, Npc npc) {
        send(viewer, new WrapperPlayServerDestroyEntities(npc.entityId(), npc.interactionId()));
        send(viewer, new WrapperPlayServerPlayerInfoRemove(List.of(npc.profileId())));
        send(viewer, new WrapperPlayServerTeams(trim16(npc.id()), TeamMode.REMOVE, Optional.empty()));
    }

    public static void rotate(Player viewer, Npc npc, float yaw, float pitch) {
        send(viewer, new WrapperPlayServerEntityRotation(npc.entityId(), yaw, pitch, true));
        send(viewer, new WrapperPlayServerEntityHeadLook(npc.entityId(), yaw));
    }

    private static void send(Player viewer, PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, wrapper);
    }

    private static String trim16(String s) { return s.length() <= 16 ? s : s.substring(0, 16); }

    private static NamedTextColor color(String key) {
        NamedTextColor c = NamedTextColor.NAMES.value(key == null ? "white" : key.toLowerCase());
        return c != null ? c : NamedTextColor.WHITE;
    }
}
