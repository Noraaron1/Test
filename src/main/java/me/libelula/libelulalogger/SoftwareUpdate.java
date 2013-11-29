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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class SoftwareUpdate of the plugin.
 *
 * @author Diego Lucio D'Onofrio <ddonofrio@member.fsf.org>
 * @version 1.0
 */
public class SoftwareUpdate extends BukkitRunnable {

    private final LibelulaLogger plugin;

    public SoftwareUpdate(LibelulaLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String updatePolicyQuery;

        switch (plugin.configuration.updatePolicy) {
            case "ALL":
                updatePolicyQuery = "/beta/";
                break;
            case "MEDIUM":
                updatePolicyQuery = "/rc/";
                break;
            case "MINOR":
                updatePolicyQuery = "/stable/";
                break;
            default:
                return;
        }
        String version = plugin.getDescription().getVersion();

        plugin.logInfo("Checking for updates.");

        String updatePath = plugin.getDataFolder().getAbsolutePath().concat("/updates");
        File updateDir = new File(updatePath);
        updateDir.mkdirs();
        try {
            String binaryFile = updatePath.concat("/LibelulaLogger.jar");
            downloadFile("http://libelula.me/updates".concat(updatePolicyQuery)
                    .concat("LibelulaLogger.jar?version=").concat(version),
                    binaryFile);

            String md5File = updatePath.concat("/LibelulaLogger.md5");
            downloadFile("http://libelula.me/updates".concat(updatePolicyQuery)
                    .concat("LibelulaLogger.md5?version=").concat(version),
                    md5File);

            if (checkUpdate(plugin)) {
                plugin.logInfo("Plugin updated, new version will be loaded on next server restart.");
                this.cancel();
            }

        } catch (Exception ex) {
            plugin.logWarning(ex.toString());
            plugin.logWarning("Unable to get updates.");
        }

    }

    public static boolean checkUpdate(Plugin plugin) {
        InputStream fis;
        try {
            fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath()
                    .concat("/updates/LibelulaLogger.md5"));
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            String hashLine = br.readLine();
            br.close();
            if (hashLine.split(" ")[0].equalsIgnoreCase(getMD5Checksum(plugin.getDataFolder()
                    .getAbsolutePath().concat("/updates/LibelulaLogger.jar")))) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private void downloadFile(String uri, String path) throws IOException {
        URL updateURI = new URL(uri);
        try (ReadableByteChannel rbc = Channels.newChannel(updateURI.openStream())) {
            FileOutputStream fos;
            fos = new FileOutputStream(path);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        }
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    private static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
