package top.mrxiaom.welcome;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.welcome.database.WelcomeDatabase;

public class WelcomeActionsPlugin extends BukkitPlugin {
    public WelcomeActionsPlugin() {
        super(options()
                .bungee(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.welcome.utils")
        );
    }

    private WelcomeDatabase welcomeDatabase;

    public WelcomeDatabase getWelcomeDatabase() {
        return welcomeDatabase;
    }

    public void reloadDatabase() {
        options.database().reloadConfig();
        options.database().reconnect();
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(welcomeDatabase = new WelcomeDatabase(this));
    }

    @Override
    protected void afterEnable() {
        getLogger().info(getDescription().getName() + " 加载完毕");
    }
}
