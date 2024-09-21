package top.mrxiaom.welcome.commands;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.DatabaseHolder;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.welcome.WelcomeActionsPlugin;
import top.mrxiaom.welcome.func.AbstractModule;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(WelcomeActionsPlugin plugin) {
        super(plugin);
        registerCommand("welcomeactions", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 2 && "hello".equalsIgnoreCase(args[0])) {
                String target = args[1];
                // TODO
            }
        }
        if (sender.isOp() && args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.reloadDatabase();
                return t(sender, "&a数据库已重载");
            }
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载 &7(重连数据库请执行 /wa reload database)");
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList(
            "hello");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(listArg0, args[0]);
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
