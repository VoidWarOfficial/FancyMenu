package de.keksuccino.fancymenu.commands;

import de.keksuccino.fancymenu.menu.variables.VariableHandler;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VariableCommand extends CommandBase {

    @Override
    public String getName() {
        return "fmvariable";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("get") && (args.length >= 2)) {
                getVariable(sender, "get", args[1]);
            }
            if (args[0].equalsIgnoreCase("set") && (args.length >= 4)) {
                boolean feedback = false;
                if (args[3].equalsIgnoreCase("true")) {
                    feedback = true;
                }
                setVariable(sender, "set", args[1], args[2], feedback);
            }
        }
    }

    private static int getVariable(ICommandSender sender, String getOrSet, String variableName) {
        try {
            if (getOrSet.equalsIgnoreCase("get")) {
                String s = VariableHandler.getVariable(variableName);
                if (s != null) {
                    sender.sendMessage(new TextComponentString(Locals.localize("fancymenu.commands.variable.get.success", s)));
                } else {
                    sender.sendMessage(new TextComponentString(Locals.localize("fancymenu.commands.variable.not_found")));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(new TextComponentString("Error while executing command!"));
            e.printStackTrace();
        }
        return 1;
    }

    private static int setVariable(ICommandSender sender, String getOrSet, String variableName, String setToValue, boolean sendFeedback) {
        try {
            if (getOrSet.equalsIgnoreCase("set")) {
                VariableHandler.setVariable(variableName, setToValue);
                if (sendFeedback) {
                    sender.sendMessage(new TextComponentString(Locals.localize("fancymenu.commands.variable.set.success", setToValue)));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(new TextComponentString("Error while executing command!"));
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> l = new ArrayList<>();
        if (args.length == 1) {
            l.add("get");
            l.add("set");
        } else if (args.length == 2) {
            l.add("<variable_name>");
        } else if (args.length == 3) {
            l.add("<set_to_value>");
        } else if (args.length == 4) {
            l.add("<send_chat_feedback>");
            l.add("true");
            l.add("false");
        }
        return l;
    }

}
