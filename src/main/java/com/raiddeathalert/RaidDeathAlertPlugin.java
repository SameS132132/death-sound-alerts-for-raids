package com.raiddeathalert;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Death Sound Alerts for Raids"
)
public class RaidDeathAlertPlugin extends Plugin
{
	private static final String TOB_ENTRY = "You enter the Theatre of Blood";

	@Inject
	private Client client;

	@Inject
	private RaidDeathAlertConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	private Raid currentRaid = Raid.NONE;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Raid Death Alert started");
		currentRaid = Raid.NONE;
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Raid Death Alert stopped");
		currentRaid = Raid.NONE;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();

		if (message.contains(TOB_ENTRY))
		{
			currentRaid = Raid.TOB;
			return;
		}

		if (message.contains(" has died. Death count: "))
		{
			handleDeath(message);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (currentRaid != Raid.TOB)
		{
			return;
		}

		if (event.getMenuOption().equals("Bank"))
		{
			currentRaid = Raid.NONE;
			log.info("Left Theatre of Blood via bank interaction");
		}
	}

	private void handleDeath(String message)
	{
		if (currentRaid == Raid.NONE)
		{
			return;
		}

		if (!isRaidEnabled())
		{
			return;
		}

		log.info("RAID DEATH DETECTED: {} | Raid: {}", message, currentRaid);

		playAlertSound();
	}

	private void playAlertSound()
	{
		RaidDeathAlertConfig.AlertSound selectedSound = config.sound();

		try
		{
			audioPlayer.play(
					RaidDeathAlertPlugin.class,
					selectedSound.getFileName(),
					0
			);
		}
		catch (Exception e)
		{
			log.error("Failed to play alert sound", e);
		}
	}

	private boolean isRaidEnabled()
	{
		return currentRaid == Raid.TOB && config.tob();
	}

	@Provides
	RaidDeathAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidDeathAlertConfig.class);
	}

	private enum Raid
	{
		NONE,
		TOB
	}
}