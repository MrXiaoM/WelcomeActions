package top.mrxiaom.welcome.func;

import top.mrxiaom.welcome.WelcomeActionsPlugin;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<WelcomeActionsPlugin> {
    public AbstractPluginHolder(WelcomeActionsPlugin plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(WelcomeActionsPlugin plugin, boolean register) {
        super(plugin, register);
    }
}
