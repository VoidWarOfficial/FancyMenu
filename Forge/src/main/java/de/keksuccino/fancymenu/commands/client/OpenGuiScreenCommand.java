package de.keksuccino.fancymenu.commands.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.guicreator.CustomGuiLoader;
import de.keksuccino.fancymenu.customization.guiconstruction.GuiConstructor;
import de.keksuccino.fancymenu.threading.MainThreadTaskExecutor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OpenGuiScreenCommand {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("openguiscreen").then(Commands.argument("menu_identifier", StringArgumentType.string())
                .executes((stack) -> {
                    return openGui(stack.getSource(), StringArgumentType.getString(stack, "menu_identifier"));
                })
                .suggests((context, provider) -> {
                    return CommandUtils.getStringSuggestions(provider, "<menu_identifier>");
                })
        ));
    }

    private static int openGui(CommandSourceStack stack, String menuIdentifierOrCustomGuiName) {
        try {
            if (menuIdentifierOrCustomGuiName.equalsIgnoreCase(CreateWorldScreen.class.getName())) {
                CreateWorldScreen.openFresh(Minecraft.getInstance(), Minecraft.getInstance().screen);
                return 1;
            }
            if (CustomGuiLoader.guiExists(menuIdentifierOrCustomGuiName)) {
                MainThreadTaskExecutor.executeInMainThread(() -> {
                    Minecraft.getInstance().setScreen(CustomGuiLoader.getGui(menuIdentifierOrCustomGuiName, Minecraft.getInstance().screen, null));
                }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
            } else {
                Screen s = GuiConstructor.tryToConstruct(ScreenCustomization.findValidMenuIdentifierFor(menuIdentifierOrCustomGuiName));
                if (s != null) {
                    MainThreadTaskExecutor.executeInMainThread(() -> {
                        Minecraft.getInstance().setScreen(s);
                    }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
                } else {
                    stack.sendFailure(Component.literal(I18n.get("fancymenu.commands.openguiscreen.cannotopen")));
                }
            }
        } catch (Exception e) {
            stack.sendFailure(Component.literal(I18n.get("fancymenu.commands.openguiscreen.error")));
            e.printStackTrace();
        }
        return 1;
    }

}