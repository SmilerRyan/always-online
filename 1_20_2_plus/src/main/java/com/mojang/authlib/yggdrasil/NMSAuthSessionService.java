package com.mojang.authlib.yggdrasil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import me.dablakbandit.ao.databases.Database;
import me.dablakbandit.ao.hybrid.IAlwaysOnline;
import me.dablakbandit.ao.utils.NMSUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NMSAuthSessionService extends YggdrasilMinecraftSessionService {

	private final IAlwaysOnline alwaysOnline;
	private final Database database;
	private final Cache<UUID, Optional<ProfileResult>> uuidNameCache;

	private final YggdrasilMinecraftSessionService oldSessionService;

	private final Method fetchProfile1;
	private final Method fetchProfile2;

	public NMSAuthSessionService(IAlwaysOnline alwaysOnline, YggdrasilMinecraftSessionService oldSessionService, ServicesKeySet servicesKeySet, Proxy proxy, Environment enviroment, Database database) {
		super(servicesKeySet, proxy, enviroment);
		this.alwaysOnline = alwaysOnline;
		this.oldSessionService = oldSessionService;
		this.database = database;
		this.fetchProfile1 = NMSUtils.getMethod(oldSessionService.getClass(), "fetchProfile", GameProfile.class, boolean.class);
		this.fetchProfile2 = NMSUtils.getMethod(oldSessionService.getClass(), "fetchProfile", UUID.class, boolean.class);
		this.uuidNameCache = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build();
	}


	public ProfileResult hasJoinedServer(String profileName, String serverId, InetAddress address) throws AuthenticationUnavailableException {
		if (alwaysOnline.isDebug()) {
			alwaysOnline.getNativeExecutor().log(Level.INFO, "Checking if " + profileName + " has joined the server with id " + serverId + " and address " + address);
		}
		if (alwaysOnline.getOfflineMode()) {
			UUID uuid = this.database.getUUID(profileName);
			if (alwaysOnline.isDebug()) {
				alwaysOnline.getNativeExecutor().log(Level.INFO, "Got uuid " + uuid + " for profile name " + profileName + " from database");
			}
			if (uuid != null) {
				this.uuidNameCache.put(uuid, Optional.of(new ProfileResult(new GameProfile(uuid, profileName))));
				return new ProfileResult(new GameProfile(uuid, profileName));
			} else {
				alwaysOnline.getNativeExecutor().log(Level.INFO, profileName + " " + "never joined this server before when mojang servers were online. Denying their access.");
				throw new AuthenticationUnavailableException("Mojang servers are offline and we can't authenticate the player with our own system.");
			}
		} else {
			return super.hasJoinedServer(profileName, serverId, address);
		}
	}


	public ProfileResult fetchProfile(GameProfile profile, boolean requireSecure) {
		if (alwaysOnline.isDebug()) {
			alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetching game profile " + profile.getName() + " with id " + profile.getId());
		}
		if (alwaysOnline.getOfflineMode()) {
			ProfileResult profileResult = Objects.requireNonNull(uuidNameCache.getIfPresent(profile.getId())).orElse(null);
			if (alwaysOnline.isDebug()) {
				alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetched game profile " + profile.getName() + " with id " + profile.getId() + " from cache: " + (profileResult != null));
			}
			return profileResult;
		}
		try {
			if (alwaysOnline.isDebug()) {
				alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetching game profile1 " + profile.getName() + " with id " + profile.getId());
			}
			return (ProfileResult) fetchProfile1.invoke(oldSessionService, profile, requireSecure);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


	public ProfileResult fetchProfile(UUID profileId, boolean requireSecure) {
		if (alwaysOnline.isDebug()) {
			alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetching uuid profile " + profileId);
		}
		if (alwaysOnline.getOfflineMode()) {
			ProfileResult profileResult = Objects.requireNonNull(uuidNameCache.getIfPresent(profileId)).orElse(null);
			if (alwaysOnline.isDebug()) {
				alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetched uuid profile " + profileId + " from cache: " + (profileResult != null));
			}
			return profileResult;
		}
		try {
			if (alwaysOnline.isDebug()) {
				alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetching uuid profile2 " + profileId + " from mojang servers");
			}
			return (ProfileResult) fetchProfile2.invoke(oldSessionService, profileId, requireSecure);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private ProfileResult fetchProfileUncached(UUID profileId, boolean requireSecure) {
		if (alwaysOnline.isDebug()) {
			alwaysOnline.getNativeExecutor().log(Level.INFO, "Fetching uncached uuid profile " + profileId);
		}
		return fetchProfile(profileId, requireSecure);
	}

}
