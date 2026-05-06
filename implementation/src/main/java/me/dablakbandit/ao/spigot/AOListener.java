package me.dablakbandit.ao.spigot;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AOListener implements Listener {

	private final Pattern pat = Pattern.compile("^[a-zA-Z0-9_-]{3,16}$");    // The regex to verify usernames;

	private final SpigotLoader spigotLoader;
	private String MOTD;

	public AOListener(SpigotLoader spigotLoader) {
		this.spigotLoader = spigotLoader;
		this.MOTD = ChatColor.translateAlternateColorCodes('&', this.spigotLoader.alwaysOnline.config.getProperty("message-motd-offline", "&eMojang servers are down,\\n&ebut you can still connect!"));
		if ("null".equals(this.MOTD)) this.MOTD = null;
	}

	// Low priority so that we can go first. ignoreCancelled is set to false to prevent some security concern.
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
	}

	/**
	 * Validate username with regular expression
	 *
	 * @param username username for validation
	 * @return true valid username, false invalid username
	 */
	public boolean validate(String username) {
		return username != null && pat.matcher(username).matches();
	}

}
