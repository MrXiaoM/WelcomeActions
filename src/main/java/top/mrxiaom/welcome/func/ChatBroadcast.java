package top.mrxiaom.welcome.func;

import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Bytes;
import top.mrxiaom.welcome.WelcomeActionsPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@AutoRegister
public class ChatBroadcast extends AbstractModule {
    private static final String CHANNEL = "WelcomeActionsChat";
    boolean receive;
    public ChatBroadcast(WelcomeActionsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        receive = config.getBoolean("actions.receiver-welcome");
    }

    public void broadcast(Player p, BaseComponent... msg) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ByteArrayDataOutput out = Bytes.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF(CHANNEL);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream msgOut = new DataOutputStream(bytes)) {
                String raw = ComponentSerializer.toString(msg);
                msgOut.writeUTF(raw);
            } catch (Throwable t) {
                warn(t);
                return;
            }
            out.writeShort(bytes.toByteArray().length);
            out.write(bytes.toByteArray());
            byte[] message = out.toByteArray();
            p.sendPluginMessage(plugin, "BungeeCord", message);
        });
    }

    @Override
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {
        if (subChannel.equalsIgnoreCase(CHANNEL)) {
            String raw = in.readUTF();
            receive(ComponentSerializer.parse(raw));
        }
    }

    public void receive(BaseComponent[] msg) {
        Bukkit.spigot().broadcast(msg);
    }

    public static ChatBroadcast inst() {
        return instanceOf(ChatBroadcast.class);
    }
}
