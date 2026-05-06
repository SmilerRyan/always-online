package me.dablakbandit.ao.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.GameProfile;
import me.dablakbandit.ao.proxy.ProxyListener;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityListener extends ProxyListener {

	private final VelocityLoader velocityLoader;

	public VelocityListener(VelocityLoader velocityLoader) {
		super(velocityLoader);
		this.velocityLoader = velocityLoader;
	}

	private void debug(String message) {
		if (velocityLoader.getAOInstance().isDebug()) {
			velocityLoader.getLogger().info("[AlwaysOnline-Debug] " + message);
		}
	}


}
