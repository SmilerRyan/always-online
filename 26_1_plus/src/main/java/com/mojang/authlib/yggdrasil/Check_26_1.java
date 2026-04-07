package com.mojang.authlib.yggdrasil;

import me.dablakbandit.ao.hybrid.IAlwaysOnline;
import me.dablakbandit.ao.utils.NMSUtils;

public class Check_26_1 {

	private static AlwaysOnlineServerConnectionListener listener;

	public static boolean valid() {
		return NMSUtils.getFieldSilent(NMSUtils.getClassSilent("net.minecraft.server.network.ServerConnectionListener"), "connections") != null;
	}

	public static void setup(IAlwaysOnline alwaysOnline) throws Exception {
		if (listener == null) {
			listener = new AlwaysOnlineServerConnectionListener(alwaysOnline);
		}
		listener.init();
	}

	public static void teardown() {
		if (listener != null) {
			listener.restore();
			listener = null;
		}
	}

}
