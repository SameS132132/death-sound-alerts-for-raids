package com.raiddeathalert;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import net.runelite.api.events.MenuOptionClicked;

import java.util.EnumMap;
import java.util.Map;

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

	private Raid currentRaid = Raid.NONE;
	private final Map<RaidDeathAlertConfig.AlertSound, Clip> soundClips = new EnumMap<>(RaidDeathAlertConfig.AlertSound.class);

	@Override
	protected void startUp() throws Exception
	{
		log.info("Raid Death Alert started");
		currentRaid = Raid.NONE;

		loadSounds();
	}

	private void loadSounds() throws Exception
	{
		for (RaidDeathAlertConfig.AlertSound alertSound : RaidDeathAlertConfig.AlertSound.values())
		{
			String fileName = alertSound.getFileName();

			try (AudioInputStream audioInputStream =
						 AudioSystem.getAudioInputStream(
								 getClass().getResourceAsStream("/" + fileName)))
			{
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);

				soundClips.put(alertSound, clip);
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Raid Death Alert stopped");
		currentRaid = Raid.NONE;

		for (Clip clip : soundClips.values())
		{
			clip.stop();
			clip.close();
		}

		soundClips.clear();
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
		Clip clip = soundClips.get(selectedSound);

		if (clip == null)
		{
			log.warn("No sound loaded for {}", selectedSound);
			return;
		}

		try
		{
			clip.stop();
			clip.setFramePosition(0);
			clip.start();
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