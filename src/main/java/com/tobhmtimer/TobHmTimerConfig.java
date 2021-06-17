package com.tobhmtimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tobhmtimer")
public interface TobHmTimerConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "showTimeToBeat",
		name = "Show time to beat",
		description = "Display the time to beat."
	)
	default boolean showTimeToBeat()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "showTimeElapsed",
		name = "Show time elapsed",
		description = "Display the time passed since the start of the raid."
	)
	default boolean showTimeElapsed()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "showTimeRemaining",
		name = "Show time remaining",
		description = "Display the time remaining until the time to beat."
	)
	default boolean showTimeRemaining()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "showTimeDescription",
		name = "Show time descriptions",
		description = "Show the description of the displayed times."
	)
	default boolean showTimeDescription()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "showSplits",
		name = "Show splits",
		description = "Show a time split for every boss."
	)
	default boolean showSplits()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "alwaysShow",
		name = "Always show",
		description = "Always display the plugin overlay, even when no raid is in progress."
	)
	default boolean alwaysShow()
	{
		return false;
	}
}
