package me.dablakbandit.ao.bungee;

import me.dablakbandit.ao.proxy.ProxyListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

public class BungeeListener extends ProxyListener implements Listener {

	private final BungeeLoader bungeeLoader;

	public BungeeListener(BungeeLoader bungeeLoader) {
		super(bungeeLoader);
		this.bungeeLoader = bungeeLoader;
		this.MOTD = ChatColor.translateAlternateColorCodes('&', this.bungeeLoader.getAOInstance().config.getProperty("message-motd-offline", "&eMojang servers are down,\\n&ebut you can still connect!"));
		if ("null".equals(this.MOTD)) this.MOTD = null;
	}

	// A high priority to allow other plugins to go first
	@EventHandler(priority = 65)
	public void onPreLogin(PreLoginEvent event) {
		// Make sure it is not canceled
		if (event.isCancelled()) return;
		if (bungeeLoader.getAOInstance().getOfflineMode()) {// Make sure we are in mojang offline mode
			// Initialize our hacky stuff
			InitialHandler handler = (InitialHandler) event.getConnection();
			// Get the connecting ip
			final String ip = handler.getAddress().getAddress().getHostAddress();
			if (ip.equals("127.0.0.1")) {
				this.bungeeLoader.getLogger().info("Skipping session login for player " + event.getConnection().getName() + " [Connected ip: " + ip + "]!");
				boolean onlineMode = handler.isOnlineMode();
				handler.setOnlineMode(false);
				UUID uuid = this.bungeeLoader.alwaysOnline.database.getUUID(event.getConnection().getName());
				if(uuid != null) {
					handler.setUniqueId(uuid);
				}
				handler.setOnlineMode(onlineMode);
			}
		}
	}

	@EventHandler(priority = -65)
	public void onLogin(LoginEvent event) {
		if (event.isCancelled()) return;
		if (bungeeLoader.getAOInstance().getOfflineMode()) {// Make sure we are in mojang offline mode
			InitialHandler handler = (InitialHandler) event.getConnection();
			final String ip = handler.getAddress().getAddress().getHostAddress();
			if (ip.equals("127.0.0.1")) {
				UUID uuid = this.bungeeLoader.alwaysOnline.database.getUUID(event.getConnection().getName());
				if (uuid != null && !uuid.equals(handler.getUniqueId())) {
					this.bungeeLoader.getLogger().info("Updating Login UUID for " + event.getConnection().getName() + " to " + uuid.toString() + "!");
					boolean onlineMode = handler.isOnlineMode();
					handler.setOnlineMode(false);
					handler.setUniqueId(uuid);
					handler.setOnlineMode(onlineMode);
				}
			}
		}
	}

	// Set priority to highest to almost guaranteed to have our MOTD displayed
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event) {
		if (bungeeLoader.getAOInstance().getOfflineMode() && this.MOTD != null) {
			ServerPing sp = event.getResponse();
			sp.setDescription(this.MOTD);
			event.setResponse(sp);
		}
	}

	@SuppressWarnings("deprecation")
	// Set priority to lowest since we'll be needing to go first
	@EventHandler(priority = -65)
	public void onPost(PostLoginEvent event) {
		if (!bungeeLoader.getAOInstance().getOfflineMode()) {
			// If we are not in mojang offline mode, update the player data
			final String username = event.getPlayer().getName();
			final String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
			final UUID uuid = event.getPlayer().getUniqueId();
			this.bungeeLoader.getProxy().getScheduler().runAsync(this.bungeeLoader, new Runnable() {
				@Override
				public void run() {
					BungeeListener.this.bungeeLoader.alwaysOnline.database.updatePlayer(username, ip, uuid);
				}
			});
		}
	}

}
