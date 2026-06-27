package com.yastro.npc.model;

import com.yastro.npc.skin.SkinData;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NpcData(
        String id,
        UUID world,
        double x, double y, double z,
        float yaw, float pitch,
        SkinSource skinSource,
        SkinData skin,
        List<String> nameLines,
        boolean lookAtPlayer,
        String model,
        List<ActionData> actions,
        boolean glow,
        String glowColor,
        double scale,
        boolean showInTab,
        String tipo,
        double nameOffset,
        String pose,
        Map<String, String> equipment,
        String visibility,
        String permissao,
        boolean skinMirror,
        boolean collidable,
        List<String> manualViewers,
        List<Double> nameScales,
        double hitOffX, double hitOffY, double hitOffZ,
        double hitWidth, double hitHeight
) {
    public NpcData withSkin(SkinData v)            { Builder b = b(); b.skin = v; return b.build(); }
    public NpcData withSkinSource(SkinSource v)    { Builder b = b(); b.skinSource = v; b.skin = SkinData.EMPTY; return b.build(); }
    public NpcData withActions(List<ActionData> v) { Builder b = b(); b.actions = v; return b.build(); }
    public NpcData withNameLines(List<String> v)   { Builder b = b(); b.nameLines = v; return b.build(); }
    public NpcData withLookAt(boolean v)           { Builder b = b(); b.lookAtPlayer = v; return b.build(); }
    public NpcData withModel(String v)             { Builder b = b(); b.model = v; return b.build(); }
    public NpcData withGlow(boolean v)             { Builder b = b(); b.glow = v; return b.build(); }
    public NpcData withGlowColor(String v)         { Builder b = b(); b.glowColor = v; return b.build(); }
    public NpcData withScale(double v)             { Builder b = b(); b.scale = v; return b.build(); }
    public NpcData withShowInTab(boolean v)        { Builder b = b(); b.showInTab = v; return b.build(); }
    public NpcData withTipo(String v)              { Builder b = b(); b.tipo = v; return b.build(); }
    public NpcData withNameOffset(double v)        { Builder b = b(); b.nameOffset = v; return b.build(); }
    public NpcData withPose(String v)              { Builder b = b(); b.pose = v; return b.build(); }
    public NpcData withEquipment(Map<String, String> v) { Builder b = b(); b.equipment = v; return b.build(); }
    public NpcData withVisibility(String v)        { Builder b = b(); b.visibility = v; return b.build(); }
    public NpcData withPermissao(String v)         { Builder b = b(); b.permissao = v; return b.build(); }
    public NpcData withSkinMirror(boolean v)       { Builder b = b(); b.skinMirror = v; return b.build(); }
    public NpcData withCollidable(boolean v)       { Builder b = b(); b.collidable = v; return b.build(); }
    public NpcData withManualViewers(List<String> v) { Builder b = b(); b.manualViewers = v; return b.build(); }
    public NpcData withNameScales(List<Double> v)  { Builder b = b(); b.nameScales = v; return b.build(); }
    public NpcData withHitbox(double ox, double oy, double oz, double w, double h) {
        Builder b = b(); b.hitOffX = ox; b.hitOffY = oy; b.hitOffZ = oz; b.hitWidth = w; b.hitHeight = h; return b.build();
    }
    public NpcData withId(String v)                { Builder b = b(); b.id = v; return b.build(); }

    public NpcData withLocation(UUID world, double x, double y, double z, float yaw, float pitch) {
        Builder b = b();
        b.world = world; b.x = x; b.y = y; b.z = z; b.yaw = yaw; b.pitch = pitch;
        return b.build();
    }

    private Builder b() { return new Builder(this); }

    private static final class Builder {
        String id; UUID world; double x, y, z; float yaw, pitch;
        SkinSource skinSource; SkinData skin; List<String> nameLines; boolean lookAtPlayer;
        String model; List<ActionData> actions; boolean glow; String glowColor; double scale;
        boolean showInTab; String tipo; double nameOffset; String pose; Map<String, String> equipment;
        String visibility; String permissao; boolean skinMirror; boolean collidable; List<String> manualViewers;
        List<Double> nameScales;
        double hitOffX, hitOffY, hitOffZ, hitWidth, hitHeight;

        Builder(NpcData d) {
            id = d.id; world = d.world; x = d.x; y = d.y; z = d.z; yaw = d.yaw; pitch = d.pitch;
            skinSource = d.skinSource; skin = d.skin; nameLines = d.nameLines; lookAtPlayer = d.lookAtPlayer;
            model = d.model; actions = d.actions; glow = d.glow; glowColor = d.glowColor; scale = d.scale;
            showInTab = d.showInTab; tipo = d.tipo; nameOffset = d.nameOffset; pose = d.pose; equipment = d.equipment;
            visibility = d.visibility; permissao = d.permissao; skinMirror = d.skinMirror; collidable = d.collidable;
            manualViewers = d.manualViewers; nameScales = d.nameScales;
            hitOffX = d.hitOffX; hitOffY = d.hitOffY; hitOffZ = d.hitOffZ; hitWidth = d.hitWidth; hitHeight = d.hitHeight;
        }

        NpcData build() {
            return new NpcData(id, world, x, y, z, yaw, pitch, skinSource, skin, nameLines, lookAtPlayer, model,
                    actions, glow, glowColor, scale, showInTab, tipo, nameOffset, pose, equipment,
                    visibility, permissao, skinMirror, collidable, manualViewers, nameScales,
                    hitOffX, hitOffY, hitOffZ, hitWidth, hitHeight);
        }
    }
}
