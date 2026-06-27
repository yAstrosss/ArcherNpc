package com.yastro.npc.interaction;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
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

        if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity w = new WrapperPlayClientInteractEntity(event);
            if (PacketEvents.getAPI().getServerManager().getVersion()
                    .isNewerThanOrEquals(ServerVersion.V_26_1)) {
                // 26.1+ (protocol 775): ataque chega pelo packet ATTACK; INTERACT_ENTITY = sempre clique direito
                trigger = "direito";
            } else {
                // <=1.21.11: a acao real vem no fio (INTERACT / ATTACK / INTERACT_AT)
                switch (w.getAction()) {
                    case ATTACK   -> trigger = "esquerdo";
                    case INTERACT -> trigger = "direito";
                    // INTERACT_AT e o packet duplicado do clique direito (vetor de hit); descartado
                    default -> { return; }
                }
            }
            entityId = w.getEntityId();
        } else if (type == PacketType.Play.Client.ATTACK) {
            // fallback: versoes antigas que ainda mandam o packet ATTACK separado
            entityId = new WrapperPlayClientAttack(event).getEntityId();
            trigger = "esquerdo";
        } else {
            return;
        }

        Npc npc = plugin.registry().byEntityId(entityId);
        if (npc == null) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        player.getScheduler().run(plugin, t -> plugin.dispatch(player, npc, trigger), null);
    }
}
