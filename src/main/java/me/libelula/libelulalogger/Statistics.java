/*
 *     This file is part of Libelula Logger plugin.
 *
 *  Libelula Logger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libelula Logger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Libelula Logger.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package me.libelula.libelulalogger;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class Statistics of the plugin.
 *
 * @author Diego Lucio D'Onofrio <ddonofrio@member.fsf.org>
 * @version 1.0
 */
public class Statistics extends BukkitRunnable {

    private final Plugin plugin;

    public Statistics(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int totalPlayers = plugin.getServer().getOfflinePlayers().length;
        int connectedPlayers = plugin.getServer().getOnlinePlayers().length;
        int loadedWorlds = plugin.getServer().getWorlds().size();
        boolean onLineMode = plugin.getServer().getOnlineMode();
        String bukkitVersion = plugin.getServer().getBukkitVersion();
        String myVersion = plugin.getDescription().getFullName();
        try {
            URL statSite;
            statSite = new URL("http://libelula.me/anonstats?LibelulaLogger="
                    .concat(myVersion)
                    .concat("&bukkit=").concat(bukkitVersion)
                    + "&p=" + connectedPlayers + "/" + totalPlayers
                    + "&w=" + loadedWorlds
                    + "&ol=" + onLineMode);
            ReadableByteChannel rbc = Channels.newChannel(statSite.openStream());
            rbc.close();
            plugin.getLogger().info("Statistics sent. Thank you for supporting Libelula Plugins.");
        } catch (Exception ex) {
            // We don't will broder the user with this useless information.
        }
        
    }
}
