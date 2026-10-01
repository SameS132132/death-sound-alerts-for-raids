package com.raiddeathalert;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RaidDeathAlertPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RaidDeathAlertPlugin.class);
		RuneLite.main(args);
	}
}