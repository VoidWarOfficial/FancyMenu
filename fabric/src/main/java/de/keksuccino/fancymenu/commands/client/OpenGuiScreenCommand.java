package de.keksuccino.fancymenu.commands.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.keksuccino.fancymenu.customization.customgui.CustomGuiHandler;
import de.keksuccino.fancymenu.customization.screen.ScreenInstanceFactory;
import de.keksuccino.fancymenu.customization.screen.identifier.ScreenIdentifierHandler;
import de.keksuccino.fancymenu.util.threading.MainThreadTaskExecutor;
import de.keksuccino.konkrete.command.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

public class OpenGuiScreenCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(ClientCommandManager.literal("openguiscreen").then(ClientCommandManager.argument("menu_identifier", StringArgumentType.string())
                .executes((stack) -> {
                    return openGui(stack.getSource(), StringArgumentType.getString(stack, "menu_identifier"));
                })
                .suggests((context, provider) -> {
                    return CommandUtils.getStringSuggestions(provider, "<menu_identifier>");
                })
        ));
    }

    private static int openGui(FabricClientCommandSource stack, String menuIdentifierOrCustomGuiName) {
        try {

            if (menuIdentifierOrCustomGuiName.equalsIgnoreCase(CreateWorldScreen.class.getName())) {
                CreateWorldScreen.openFresh(Minecraft.getInstance(), Minecraft.getInstance().screen);
                return 1;
            }
            
            if (CustomGuiHandler.guiExists(menuIdentifierOrCustomGuiName)) {
                MainThreadTaskExecutor.executeInMainThread(() -> {
                    Minecraft.getInstance().setScreen(CustomGuiHandler.constructInstance(menuIdentifierOrCustomGuiName, Minecraft.getInstance().screen, null));
                }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
            } else {
                Screen s = ScreenInstanceFactory.tryConstruct(ScreenIdentifierHandler.getBestIdentifier(menuIdentifierOrCustomGuiName));
                if (s != null) {
                    MainThreadTaskExecutor.executeInMainThread(() -> {
                        Minecraft.getInstance().setScreen(s);
                    }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
                } else {
                    stack.sendError(Component.translatable("fancymenu.commmands.openguiscreen.unable_to_open_gui", menuIdentifierOrCustomGuiName));
                }
            }
        } catch (Exception e) {
            stack.sendError(Component.translatable("fancymenu.commands.openguiscreen.error"));
            e.printStackTrace();
        }
        return 1;
    }

}