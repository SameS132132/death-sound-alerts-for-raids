package com.raiddeathalert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raiddeathalert")
public interface RaidDeathAlertConfig extends Config
{
	@ConfigItem(
			keyName = "tob",
			name = "Theatre of Blood",
			description = "Play an alert when another player dies in Theatre of Blood",
			section = "raids"
	)
	default boolean tob()
	{
		return true;
	}

	@ConfigItem(
			keyName = "sound",
			name = "Alert sound",
			description = "Choose the sound played when another player dies"
	)
	default AlertSound sound()
	{
		return AlertSound.DEFAULT;
	}

	enum AlertSound
	{
		DEFAULT("default.wav"),
		AIRHORN("airhorn.wav"),
		MEOW("meow.wav"),
		OLDMAN("oldman.wav"),
		QUACK("quack.wav"),
		GUNSHOT("gunshot.wav");

		private final String fileName;

		AlertSound(String fileName)
		{
			this.fileName = fileName;
		}

		public String getFileName()
		{
			return fileName;
		}

		@Override
		public String toString()
		{
			switch (this)
			{
				case DEFAULT:
					return "Default";

				case AIRHORN:
					return "Airhorn";

				case MEOW:
					return "Meow";

				case OLDMAN:
					return "Old Man";

				case QUACK:
					return "Quack";

				case GUNSHOT:
					return "Gunshot";

				default:
					return name();
			}
		}
	}
}