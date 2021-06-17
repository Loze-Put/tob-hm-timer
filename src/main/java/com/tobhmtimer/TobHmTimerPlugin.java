package com.tobhmtimer;

import com.google.inject.Provides;
import static com.tobhmtimer.TobHmTimerConstants.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "TOB Hard Mode Timer"
)
public class TobHmTimerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TobHmTimerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TobHmTimerOverlay overlay;

	private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

	@Getter
	private boolean insideTob = false;
	@Getter
	private boolean raidActive = false;
	@Getter
	private boolean raidSucceeded = false;

	private int tobVarbit = 0;
	private long ticksElapsed = 0;

	@Getter
	private Duration timeToBeat = Duration.ZERO;
	@Getter
	private Duration splitMaiden;
	@Getter
	private Duration splitBloat;
	@Getter
	private Duration splitNylocas;
	@Getter
	private Duration splitSotetseg;
	@Getter
	private Duration splitXarpus;
	@Getter
	private Duration splitVerzik;

	public Duration getElapsedTime()
	{
		return getDurationFromTicks(ticksElapsed);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		reset();
		insideTob = false;
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (message.startsWith(MESSAGE_RAID_STARTED))
		{
			Duration time = parseTime(message);

			if (time != null)
			{
				timeToBeat = time;
				startRaid();
			}

			return;
		}

		if (!raidActive)
		{
			return;
		}

		if (message.startsWith(MESSAGE_SPLIT_MAIDEN))
		{
			splitMaiden = getDurationFromTicks(ticksElapsed);
		}
		else if (message.startsWith(MESSAGE_SPLIT_BLOAT))
		{
			splitBloat = getDurationFromTicks(ticksElapsed);
		}
		else if (message.startsWith(MESSAGE_SPLIT_NYLOCAS))
		{
			splitNylocas = getDurationFromTicks(ticksElapsed);
		}
		else if (message.startsWith(MESSAGE_SPLIT_SOTETSEG))
		{
			splitSotetseg = getDurationFromTicks(ticksElapsed);
		}
		else if (message.startsWith(MESSAGE_SPLIT_XARPUS))
		{
			splitXarpus = getDurationFromTicks(ticksElapsed);
		}
		else if (message.startsWith(MESSAGE_RAID_COMPLETED))
		{
			splitVerzik = getDurationFromTicks(ticksElapsed);
			completeRaid(true);
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (raidActive)
		{
			ticksElapsed++;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int nextTobVarBit = client.getVar(Varbits.THEATRE_OF_BLOOD);

		if (tobVarbit != nextTobVarBit)
		{
			// The player walked into the theatre.
			if (tobVarbit == STATE_IN_PARTY && nextTobVarBit == STATE_INSIDE_OR_SPECTATING)
			{
				insideTob = true;
			}

			// The player has left the theatre.
			else if (nextTobVarBit == STATE_IN_PARTY && raidActive)
			{
				completeRaid(false);
			}

			// The player has left their party.
			else if (nextTobVarBit == STATE_NO_PARTY)
			{
				insideTob = false;
				reset();
			}

			tobVarbit = nextTobVarBit;
		}
	}

	@Provides
	TobHmTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobHmTimerConfig.class);
	}

	private Duration parseTime(String timeString)
	{
		Matcher digitMatcher = DIGIT_PATTERN.matcher(timeString);

		ArrayList<String> digits = new ArrayList<String>();
		while (digitMatcher.find())
		{
			digits.add(digitMatcher.group());
		}

		if (digits.size() == 2)
		{
			return Duration.ofMinutes(Integer.parseInt(digits.get(0)))
				.plus(Duration.ofSeconds(Integer.parseInt(digits.get(1))));
		}

		if (digits.size() == 3)
		{
			return Duration.ofHours(Integer.parseInt(digits.get(0)))
				.plus(Duration.ofMinutes(Integer.parseInt(digits.get(1))))
				.plus(Duration.ofSeconds(Integer.parseInt(digits.get(2))));
		}

		return null;
	}

	private void startRaid()
	{
		reset();
		raidActive = true;
	}

	private void completeRaid(boolean succeeded)
	{
		raidActive = false;
		raidSucceeded = succeeded;
	}

	private void reset()
	{
		ticksElapsed = 0;
		raidActive = false;
		raidSucceeded = false;
		splitMaiden = null;
		splitBloat = null;
		splitNylocas = null;
		splitSotetseg = null;
		splitXarpus = null;
		splitVerzik = null;
	}

	private static Duration getDurationFromTicks(long ticks)
	{
		return Duration.ofMillis(MILLIS_PER_TICK * ticks);
	}
}
