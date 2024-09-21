package top.mrxiaom.welcome.database;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sqlhelper.*;
import top.mrxiaom.sqlhelper.conditions.Condition;
import top.mrxiaom.sqlhelper.conditions.EnumConditions;
import top.mrxiaom.sqlhelper.conditions.EnumOperators;
import top.mrxiaom.welcome.WelcomeActionsPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.TreeSet;

import static top.mrxiaom.pluginbase.utils.Util.stackTraceToString;

public class WelcomeDatabase implements IDatabase {
    public static String TABLE_NEWCOMER_NAME;
    public static String TABLE_NEWCOMER_DATA_NAME;
    final WelcomeActionsPlugin plugin;
    final Set<String> expiredCache = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    final Set<String> welcomeCache = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public WelcomeDatabase(WelcomeActionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload(Connection conn, String tablePrefix) {
        TABLE_NEWCOMER_NAME = (tablePrefix + "WELCOME").toUpperCase();
        TABLE_NEWCOMER_DATA_NAME = (tablePrefix + "WELCOME_DATA").toUpperCase();
        SQLang.createTable(conn, WelcomeDatabase.TABLE_NEWCOMER_NAME, true,
                TableColumn.create(SQLValueType.string(64), "newcomer", EnumConstraints.PRIMARY_KEY),
                TableColumn.create(SQLValueType.bigInt(), "expire_time")
        );
        SQLang.createTable(conn, WelcomeDatabase.TABLE_NEWCOMER_DATA_NAME, true,
                TableColumn.create(SQLValueType.string(64), "newcomer"),
                TableColumn.create(SQLValueType.string(64), "player")
        );
    }

    public void putWelcomeExpireTime(String newcomer, long expireTime) {
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = SQLang.insertInto(TABLE_NEWCOMER_NAME)
                    .addValues(
                            Pair.of("newcomer", newcomer),
                            Pair.of("expire_time", expireTime)
                    ).build(conn)
            ) {
                ps.executeUpdate();
            }
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
        }
    }

    public void putWelcomeData(String player, String newcomer) {
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = SQLang.insertInto(TABLE_NEWCOMER_DATA_NAME)
                    .addValues(
                            Pair.of("player", player),
                            Pair.of("newcomer", newcomer)
                    ).build(conn)
            ) {
                ps.executeUpdate();
                welcomeCache.add(newcomer + "," + player);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
        }
    }

    public boolean hasWelcomed(String player, String newcomer) {
        if (welcomeCache.contains(newcomer + "," + player)) return true;
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = SQLang.select(TABLE_NEWCOMER_DATA_NAME)
                    .where(
                            Condition.of("player", EnumOperators.EQUALS, player),
                            EnumConditions.AND,
                            Condition.of("newcomer", EnumOperators.EQUALS, newcomer)
                    ).limit(1).build(conn)
            ) {
                try (ResultSet result = ps.executeQuery()) {
                    welcomeCache.add(newcomer + "," + player);
                    return result.next();
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
        }
        return false;
    }

    public int getNewcomerBeWelcomedTimes(String newcomer) {
        int times = 0;
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = SQLang.select(TABLE_NEWCOMER_DATA_NAME)
                    .where(
                            Condition.of("newcomer", EnumOperators.EQUALS, newcomer)
                    ).build(conn)
            ) {
                try (ResultSet ignored = ps.executeQuery()) {
                    times++;
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
        }
        return times;
    }

    @Nullable
    public Long getWelcomeExpireTime(String newcomer) {
        if (expiredCache.contains(newcomer)) return null;
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = SQLang.select(TABLE_NEWCOMER_NAME)
                    .where(
                            Condition.of("newcomer", EnumOperators.EQUALS, newcomer)
                    ).limit(1).build(conn)
            ) {
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        Long expireTime = Util.parseLong(result.getObject("expire_time").toString())
                                .filter(time -> System.currentTimeMillis() < time)
                                .orElse(null);
                        if (expireTime == null) {
                            expiredCache.add(newcomer);
                        }
                        return expireTime;
                    }
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
        }
        return 0L;
    }
}
