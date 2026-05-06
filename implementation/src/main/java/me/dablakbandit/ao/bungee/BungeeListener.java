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
	}

	private void debug(String message) {
		if (bungeeLoader.getAOInstance().isDebug()) {
			bungeeLoader.getLogger().info("[AlwaysOnline-Debug] " + message);
		}
	}


}
