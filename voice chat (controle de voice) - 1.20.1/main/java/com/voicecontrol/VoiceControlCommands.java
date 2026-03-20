package com.voicecontrol;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class VoiceControlCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("voicecontrole")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("mutar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(VoiceControlCommands::mutar)))

                        .then(Commands.literal("desmutar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(VoiceControlCommands::desmutar)))

                        .then(Commands.literal("whitelist")
                                .then(Commands.literal("adicionar")
                                        .then(Commands.argument("alvos", EntityArgument.players())
                                                .executes(VoiceControlCommands::whitelistAdicionar)))
                                .then(Commands.literal("remover")
                                        .then(Commands.argument("alvos", EntityArgument.players())
                                                .executes(VoiceControlCommands::whitelistRemover))))

                        .then(Commands.literal("voz-universal")
                                .then(Commands.literal("adicionar")
                                        .then(Commands.argument("alvos", EntityArgument.players())
                                                .executes(VoiceControlCommands::vozUniversalAdicionar)))
                                .then(Commands.literal("remover")
                                        .then(Commands.argument("alvos", EntityArgument.players())
                                                .executes(VoiceControlCommands::vozUniversalRemover))))

                        .then(Commands.literal("lista")
                                .then(Commands.literal("mutados")
                                        .executes(VoiceControlCommands::listaMutados))
                                .then(Commands.literal("voz-universal")
                                        .executes(VoiceControlCommands::listaVozUniversal))
                                .then(Commands.literal("whitelist")
                                        .executes(VoiceControlCommands::listaWhitelist)))
        );
    }

    // ==================== MUTAR / DESMUTAR ====================

    private static int mutar(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.mutePlayer(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voce foi §cmutado §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int desmutar(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.unmutePlayer(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voce foi §adesmutado §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== WHITELIST ====================

    private static int whitelistAdicionar(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.addToWhitelist(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voce foi adicionado a §awhitelist §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int whitelistRemover(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.removeFromWhitelist(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voce foi removido da §cwhitelist §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== VOZ UNIVERSAL ====================

    private static int vozUniversalAdicionar(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.enableBroadcast(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voz universal §aativada §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int vozUniversalRemover(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> alvos = EntityArgument.getPlayers(context, "alvos");
            String nomeAdmin = context.getSource().getTextName();

            for (ServerPlayer player : alvos) {
                MuteManager.disableBroadcast(player.getUUID());

                if (player.hasPermissions(2)) {
                    player.sendSystemMessage(
                            Component.literal("§7Voz universal §cremovida §7por §f" + nomeAdmin + "§7."));
                }
            }

            return alvos.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== LISTAS ====================

    private static int listaMutados(CommandContext<CommandSourceStack> context) {
        Set<UUID> mutados = MuteManager.getMutedPlayers();

        context.getSource().sendSuccess(() ->
                Component.literal("§6========== Jogadores Mutados =========="), false);

        if (mutados.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Nenhum jogador mutado."), false);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Quantidade: §f" + mutados.size()), false);

            StringBuilder nomes = new StringBuilder();
            var server = context.getSource().getServer();

            for (UUID uuid : mutados) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    if (nomes.length() > 0) nomes.append("§7, ");
                    nomes.append("§c").append(player.getName().getString());
                }
            }

            if (nomes.length() > 0) {
                String nomesStr = nomes.toString();
                context.getSource().sendSuccess(() ->
                        Component.literal("§7Jogadores: " + nomesStr), false);
            }
        }

        context.getSource().sendSuccess(() ->
                Component.literal("§6========================================"), false);

        return 1;
    }

    private static int listaVozUniversal(CommandContext<CommandSourceStack> context) {
        Set<UUID> broadcasting = MuteManager.getBroadcastPlayers();

        context.getSource().sendSuccess(() ->
                Component.literal("§b========== Voz Universal =========="), false);

        if (broadcasting.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Nenhum jogador com voz universal."), false);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Quantidade: §f" + broadcasting.size()), false);

            StringBuilder nomes = new StringBuilder();
            var server = context.getSource().getServer();

            for (UUID uuid : broadcasting) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    if (nomes.length() > 0) nomes.append("§7, ");
                    nomes.append("§b").append(player.getName().getString());
                }
            }

            if (nomes.length() > 0) {
                String nomesStr = nomes.toString();
                context.getSource().sendSuccess(() ->
                        Component.literal("§7Jogadores: " + nomesStr), false);
            }
        }

        context.getSource().sendSuccess(() ->
                Component.literal("§b====================================="), false);

        return 1;
    }

    private static int listaWhitelist(CommandContext<CommandSourceStack> context) {
        Set<UUID> whitelist = MuteManager.getWhitelistedPlayers();

        context.getSource().sendSuccess(() ->
                Component.literal("§a========== Whitelist =========="), false);

        if (whitelist.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Nenhum jogador na whitelist."), false);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Quantidade: §f" + whitelist.size()), false);

            StringBuilder nomes = new StringBuilder();
            var server = context.getSource().getServer();

            for (UUID uuid : whitelist) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    if (nomes.length() > 0) nomes.append("§7, ");
                    nomes.append("§a").append(player.getName().getString());
                }
            }

            if (nomes.length() > 0) {
                String nomesStr = nomes.toString();
                context.getSource().sendSuccess(() ->
                        Component.literal("§7Jogadores: " + nomesStr), false);
            }
        }

        context.getSource().sendSuccess(() ->
                Component.literal("§a================================"), false);

        return 1;
    }
}