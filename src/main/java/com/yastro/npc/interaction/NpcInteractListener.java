package com.yastro.npc.interaction;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.yastro.npc.YAstroNpcPlugin;
import com.yastro.npc.model.Npc;
import org.bukkit.entity.Player;

public final class NpcInteractListener extends PacketListenerAbstract {
    private final YAstroNpcPlugin plugin;

    public NpcInteractListener(YAstroNpcPlugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        var type = event.getPacketType();
        int entityId;
        String trigger;
        if (type == PacketType.Play.Client.ATTACK) {
            entityId = new WrapperPlayClientAttack(event).getEntityId();
            trigger = "esquerdo";
        } else if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            entityId = new WrapperPlayClientInteractEntity(event).getEntityId();
            trigger = "direito";
        } else {
            return;
        }

        Npc npc = plugin.registry().byEntityId(entityId);
        if (npc == null) return;

        Player player = (Player) event.getPlayer();
        if (player == null) return;
        player.getScheduler().run(plugin, t -> plugin.dispatch(player, npc, trigger), null);
    }
}
