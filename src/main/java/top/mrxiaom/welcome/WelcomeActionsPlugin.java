package top.mrxiaom.welcome;

import top.mrxiaom.pluginbase.BukkitPlugin;

public class WelcomeActionsPlugin extends BukkitPlugin {
    public static WelcomeActionsPlugin getInstance() {
        return (WelcomeActionsPlugin) BukkitPlugin.getInstance();
    }

    public WelcomeActionsPlugin() {
        super(options()
                .bungee(false)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.welcome.utils")
        );
    }

    public void reloadDatabase() {
        options.database().reloadConfig();
        options.database().reconnect();
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(
                // TODO: 数据库
        );
    }

    @Override
    protected void afterEnable() {
        getLogger().info(getDescription().getName() + " 加载完毕");
    }
}
