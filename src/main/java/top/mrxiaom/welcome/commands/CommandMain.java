package top.mrxiaom.welcome.commands;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.welcome.WelcomeActionsPlugin;
import top.mrxiaom.welcome.func.AbstractModule;
import top.mrxiaom.welcome.func.ChatBroadcast;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static top.mrxiaom.pluginbase.utils.ColorHelper.parseColor;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter {
    long rewardExpireTimeSeconds;
    String message;
    String buttonText;
    String buttonHoverText;
    boolean welcomeEnable;
    List<String> welcomeChat;
    boolean welcomeSelf;
    int welcomeTimesLimit;
    List<String> welcomeReward;
    List<String> welcomeExpire;
    List<String> welcomeAlready;
    List<String> welcomeLimit;
    AtomicBoolean welcomeFlag = new AtomicBoolean(false);
    public CommandMain(WelcomeActionsPlugin plugin) {
        super(plugin);
        registerCommand("welcomeactions", this);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        rewardExpireTimeSeconds = config.getLong("register.reward-expire-time-seconds");
        message = config.getString("register.message");
        buttonText = config.getString("register.button.text");
        buttonHoverText = config.getString("register.button.hover");

        welcomeEnable = config.getBoolean("join-welcome.enable", false);
        welcomeChat = config.getStringList("join-welcome.chat");
        welcomeSelf = config.getBoolean("join-welcome.welcome-self", false);
        welcomeTimesLimit = config.getInt("join-welcome.times-limit", -1);
        welcomeReward = config.getStringList("join-welcome.reward-commands");
        welcomeExpire = config.getStringList("join-welcome.expire-commands");
        welcomeAlready = config.getStringList("join-welcome.already-commands");
        welcomeLimit = config.getStringList("join-welcome.limit-commands");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 2 && "hello".equalsIgnoreCase(args[0])) {
                if (welcomeChat.isEmpty()) return true;
                if (welcomeFlag.get()) {
                    player.sendMessage("§7全局冷却中，请稍候再试");
                    return true;
                }
                welcomeFlag.set(true);
                String target = args[1];
                String chat = welcomeChat.size() == 1
                        ? welcomeChat.get(0)
                        : welcomeChat.get(new Random().nextInt(welcomeChat.size()));
                if (!welcomeEnable) {
                    player.chat(chat.replace("%newcomer%", target));
                    welcomeFlag.set(false);
                    return true;
                }
                if (!welcomeSelf && player.getName().equalsIgnoreCase(target)) {
                    welcomeFlag.set(false);
                    return t(player, "&7按理来说，你不需要欢迎你自己…");
                }
                player.chat(chat.replace("%newcomer%", target));
                Long expireTime = plugin.getWelcomeDatabase().getWelcomeExpireTime(target);
                if (expireTime != null) {
                    if (expireTime <= 0L) {
                        welcomeFlag.set(false);
                        return true;
                    }
                    if (System.currentTimeMillis() > expireTime) {
                        Util.runCommands(player, welcomeExpire);
                    } else {
                        if (welcomeTimesLimit == -1 || plugin.getWelcomeDatabase().getNewcomerBeWelcomedTimes(target) < welcomeTimesLimit) {
                            if (!plugin.getWelcomeDatabase().hasWelcomed(player.getName(), target)) {
                                plugin.getWelcomeDatabase().putWelcomeData(player.getName(), target);
                                Util.runCommands(player, welcomeReward);
                            } else {
                                Util.runCommands(player, welcomeAlready);
                            }
                        } else {
                            Util.runCommands(player, welcomeLimit);
                        }
                    }
                } else {
                    Util.runCommands(player, welcomeExpire);
                }
                welcomeFlag.set(false);
                return true;
            }
        }
        if (sender.isOp()) {
            if (args.length == 2 && "register".equalsIgnoreCase(args[0])) {
                Player player = Util.getOnlinePlayer(args[1]).orElse(null);
                if (player == null || !player.isOnline()) {
                    return t(sender, "&e该玩家 &7(" + args[1] + ") &e不在线");
                }
                Long expireTime = plugin.getWelcomeDatabase().getWelcomeExpireTime(player.getName());
                if (expireTime == null || expireTime > 114514) {
                    return t(sender, "&e该玩家 &7(" + player.getName() + ") 已经注册过了");
                }
                plugin.getWelcomeDatabase().putWelcomeExpireTime(player.getName(), System.currentTimeMillis() + (rewardExpireTimeSeconds * 1000L));
                TextComponent msg = withColor(PlaceholderAPI.setPlaceholders(player, message));
                TextComponent click = withColor(buttonText);
                click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color(buttonHoverText))));
                click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/welcomeactions hello " + player.getName()));
                Bukkit.spigot().broadcast(msg, click);
                ChatBroadcast.inst().broadcast(player, msg, click);
                return t(sender, "&a已发送 &e" + player.getName() + " &a的欢迎新人消息");
            }
            if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
                if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                    plugin.reloadDatabase();
                    return t(sender, "&a数据库已重载");
                }
                plugin.reloadConfig();
                return t(sender, "&a配置文件已重载 &7(重连数据库请执行 /wa reload database)");
            }
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList("register", "reload");
    private static final List<String> listArg1Reload = Lists.newArrayList("register", "reload");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.isOp()) {
            if (args.length == 1) {
                return startsWith(listArg0, args[0]);
            }
            if (args.length == 2 && "reload".equalsIgnoreCase(args[0])) {
                return startsWith(listArg1Reload, args[1]);
            }
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
    public static String color(String s) {
        return parseColor(s.replace("&x", "§x"));
    }

    public static TextComponent text(String s) {
        return new TextComponent(TextComponent.fromLegacyText(s));
    }

    public static TextComponent withColor(String s) {
        return text(color(s));
    }
}
