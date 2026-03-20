package com.lootrnerf.command;

import com.lootrnerf.LootrNerf;
import com.lootrnerf.data.FirstOpenerTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Comandos administrativos para o LootrNerf
 */
@Mod.EventBusSubscriber(modid = LootrNerf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootrNerfCommands {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(
            Commands.literal("lootrnerf")
                .requires(source -> source.hasPermission(2)) // Requer OP level 2
                
                // Subcomando: status
                .then(Commands.literal("status")
                    .executes(LootrNerfCommands::showStatus))
                
                // Subcomando: reset <pos>
                .then(Commands.literal("reset")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(LootrNerfCommands::resetContainer)))
                
                // Subcomando: check <pos>
                .then(Commands.literal("check")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(LootrNerfCommands::checkContainer)))
                
                // Subcomando: stats
                .then(Commands.literal("stats")
                    .executes(LootrNerfCommands::showStats))
                
                // Subcomando: reload
                .then(Commands.literal("reload")
                    .executes(LootrNerfCommands::reloadConfig))
        );
    }
    
    /**
     * Mostra status geral do mod
     */
    private static int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("§6=== LootrNerf Status ==="), false);
        source.sendSuccess(() -> Component.literal("§7Mod ativo e funcionando!"), false);
        source.sendSuccess(() -> Component.literal("§7Use §f/lootrnerf stats §7para estatísticas"), false);
        source.sendSuccess(() -> Component.literal("§7Use §f/lootrnerf check <pos> §7para verificar um baú"), false);
        source.sendSuccess(() -> Component.literal("§7Use §f/lootrnerf reset <pos> §7para resetar um baú"), false);
        
        return 1;
    }
    
    /**
     * Reseta um container específico (remove o registro de primeiro jogador)
     */
    private static int resetContainer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
            ServerLevel level = source.getLevel();
            
            String key = FirstOpenerTracker.getKey(level.dimension(), pos);
            FirstOpenerTracker tracker = FirstOpenerTracker.get(level);
            
            if (tracker.hasBeenOpened(key)) {
                tracker.clearOpener(key);
                source.sendSuccess(() -> Component.literal(
                    "§aContainer em " + pos.toShortString() + " foi resetado!"
                ), true);
                source.sendSuccess(() -> Component.literal(
                    "§7O próximo jogador a abrir será considerado o primeiro."
                ), false);
            } else {
                source.sendSuccess(() -> Component.literal(
                    "§eEste container ainda não foi aberto por ninguém."
                ), false);
            }
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cErro ao resetar container: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Verifica informações de um container
     */
    private static int checkContainer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
            ServerLevel level = source.getLevel();
            
            String key = FirstOpenerTracker.getKey(level.dimension(), pos);
            FirstOpenerTracker tracker = FirstOpenerTracker.get(level);
            
            source.sendSuccess(() -> Component.literal("§6=== Informações do Container ==="), false);
            source.sendSuccess(() -> Component.literal("§7Posição: §f" + pos.toShortString()), false);
            source.sendSuccess(() -> Component.literal("§7Dimensão: §f" + level.dimension().location()), false);
            
            if (tracker.hasBeenOpened(key)) {
                UUID firstOpener = tracker.getFirstOpener(key);
                long timestamp = tracker.getOpenTimestamp(key);
                
                String timeAgo = formatTimeAgo(timestamp);
                
                source.sendSuccess(() -> Component.literal("§7Status: §cJá aberto"), false);
                source.sendSuccess(() -> Component.literal("§7Primeiro jogador: §f" + firstOpener), false);
                source.sendSuccess(() -> Component.literal("§7Aberto há: §f" + timeAgo), false);
            } else {
                source.sendSuccess(() -> Component.literal("§7Status: §aNunca aberto"), false);
            }
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cErro ao verificar container: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Mostra estatísticas gerais
     */
    private static int showStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerLevel level = source.getLevel();
            FirstOpenerTracker tracker = FirstOpenerTracker.get(level);
            
            int trackedContainers = tracker.getTrackedContainerCount();
            
            source.sendSuccess(() -> Component.literal("§6=== LootrNerf Estatísticas ==="), false);
            source.sendSuccess(() -> Component.literal("§7Containers rastreados: §f" + trackedContainers), false);
            
            // Informações de configuração
            source.sendSuccess(() -> Component.literal("§6=== Configurações Atuais ==="), false);
            source.sendSuccess(() -> Component.literal(
                "§7Chance primeiro jogador: §a" + com.lootrnerf.config.LootrNerfConfig.FIRST_OPENER_CHANCE.get() + "%"
            ), false);
            source.sendSuccess(() -> Component.literal(
                "§7Chance outros jogadores: §c" + com.lootrnerf.config.LootrNerfConfig.OTHER_OPENER_CHANCE.get() + "%"
            ), false);
            source.sendSuccess(() -> Component.literal(
                "§7Multiplicador loot (outros): §e" + com.lootrnerf.config.LootrNerfConfig.OTHER_OPENER_LOOT_MULTIPLIER.get() + "%"
            ), false);
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cErro ao obter estatísticas: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Recarrega configurações (informativo)
     */
    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal(
            "§eAs configurações do Forge são recarregadas automaticamente."
        ), false);
        source.sendSuccess(() -> Component.literal(
            "§7Edite o arquivo §fconfig/lootrnerf-server.toml §7e as mudanças serão aplicadas."
        ), false);
        
        return 1;
    }
    
    /**
     * Formata tempo decorrido
     */
    private static String formatTimeAgo(long timestamp) {
        if (timestamp == 0) return "desconhecido";
        
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + " dia(s)";
        if (hours > 0) return hours + " hora(s)";
        if (minutes > 0) return minutes + " minuto(s)";
        return seconds + " segundo(s)";
    }
}
