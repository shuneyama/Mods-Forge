package net.shune.command;

import net.shune.LuasMod;
import net.shune.LuasMod.TipoLua;
import net.shune.network.LuaNetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LuasMod.MODID)
public class LuaCommands {
    
    private static void notificarJogadores(CommandSourceStack source, TipoLua lua) {
        String mensagem = switch (lua) {
            case NORMAL -> null;
            case DEUSA -> "§6✦ A Lua da Deusa brilha no céu! ✦";
            case SORTE -> "§9✦ A Lua da Sorte abençoa esta noite! ✦";
            case VERMELHA -> "§c⚠ A Lua Vermelha surge... Cuidado! ⚠";
            case SUPER_VERMELHA -> "§4§l☠ A SUPER LUA VERMELHA ASCENDE! PERIGO! ☠";
        };
        
        if (mensagem != null && source.getServer() != null) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                player.displayClientMessage(Component.literal(mensagem), false);
            }
        }
    }
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("shune")
            .requires(source -> source.hasPermission(2))
            
            .then(Commands.literal("lua")
                
                .then(Commands.literal("set")
                    .then(Commands.argument("tipo", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (TipoLua tipo : TipoLua.values()) {
                                builder.suggest(tipo.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if (!LuasMod.isLicencaValida()) {
                                context.getSource().sendFailure(
                                    Component.literal("§cMod desativado - Licença inválida")
                                );
                                return 0;
                            }
                            
                            String tipoStr = StringArgumentType.getString(context, "tipo");
                            try {
                                TipoLua novaLua = TipoLua.valueOf(tipoStr.toUpperCase());
                                LuasMod.luaAtual = novaLua;
                                LuaNetworkHandler.syncToAll(novaLua);
                                
                                notificarJogadores(context.getSource(), novaLua);
                                
                                String mensagemAdmin = switch (novaLua) {
                                    case NORMAL -> "§7Lua voltou ao normal";
                                    case DEUSA -> "§6Lua da Deusa ativada!";
                                    case SORTE -> "§9Lua da Sorte ativada!";
                                    case VERMELHA -> "§cLua Vermelha ativada!";
                                    case SUPER_VERMELHA -> "§4Super Lua Vermelha ativada!";
                                };
                                
                                context.getSource().sendSuccess(
                                    () -> Component.literal(mensagemAdmin), 
                                    true
                                );
                                return 1;
                            } catch (IllegalArgumentException e) {
                                context.getSource().sendFailure(
                                    Component.literal("§cTipo de lua inválido! Use: normal, deusa, sorte, vermelha, super_vermelha")
                                );
                                return 0;
                            }
                        })
                    )
                )
                
                .then(Commands.literal("info")
                    .executes(context -> {
                        if (!LuasMod.isLicencaValida()) {
                            context.getSource().sendFailure(
                                Component.literal("§cMod desativado - Licença inválida")
                            );
                            return 0;
                        }
                        
                        String info = String.format(
                            "§e=== Info das Luas ===\n" +
                            "§fLua atual: %s\n" +
                            "§f\n" +
                            "§6Lua da Deusa: §7Visual amarelo\n" +
                            "§9Lua da Sorte: §7Sorte II para jogadores\n" +
                            "§cLua Vermelha: §7Mobs +25%% dano, +força, +10%% spawn\n" +
                            "§4Super Lua: §7Mobs +75%% dano, +força II, +velocidade, +20%% spawn",
                            LuasMod.luaAtual.name()
                        );
                        context.getSource().sendSuccess(
                            () -> Component.literal(info), 
                            false
                        );
                        return 1;
                    })
                )
            )
        );
    }
}
