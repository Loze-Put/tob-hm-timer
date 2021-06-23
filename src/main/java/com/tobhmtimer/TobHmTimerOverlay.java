package com.tobhmtimer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

class TobHmTimerOverlay extends OverlayPanel
{
	private final TobHmTimerPlugin plugin;
	private final TobHmTimerConfig config;

	private String longestString;

	@Inject
	private TobHmTimerOverlay(TobHmTimerPlugin plugin, TobHmTimerConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
		setPriority(OverlayPriority.LOW);
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isShowOverlay() && !config.alwaysShow())
		{
			return null;
		}

		longestString = "";

		Duration timeToBeat = plugin.getTimeToBeat();
		Duration elapsedTime = plugin.getTimer().getRealTime();

		Color timeColor = Color.WHITE;

		if (elapsedTime.compareTo(timeToBeat) >= 0 && !timeToBeat.isZero())
		{
			timeColor = Color.RED;
		}
		else if (!plugin.getTimer().isActive() && plugin.isRaidSucceeded())
		{
			timeColor = Color.GREEN;
		}

		if (config.showTimeToBeat())
		{
			panelComponent.getChildren().add(
				createLineComponent("To beat", timeToBeat)
			);
		}

		if (config.showTimeElapsed())
		{
			panelComponent.getChildren().add(
				createLineComponent("Elapsed", elapsedTime, timeColor)
			);
		}

		if (config.showTimeRemaining())
		{
			Duration timeRemaining = timeToBeat.minus(elapsedTime);
			panelComponent.getChildren().add(
				createLineComponent("Remaining", timeRemaining, timeColor)
			);
		}

		if (config.showSplits())
		{
			if (config.showTimeToBeat() || config.showTimeElapsed() || config.showTimeRemaining())
			{
				panelComponent.getChildren().add(LineComponent.builder().build());
			}

			panelComponent.getChildren().add(
				createLineComponent("Maiden", plugin.getSplitMaiden())
			);
			panelComponent.getChildren().add(
				createLineComponent("Bloat", plugin.getSplitBloat())
			);
			panelComponent.getChildren().add(
				createLineComponent("Nylocas", plugin.getSplitNylocas())
			);
			panelComponent.getChildren().add(
				createLineComponent("Sotetseg", plugin.getSplitSotetseg())
			);
			panelComponent.getChildren().add(
				createLineComponent("Xarpus", plugin.getSplitXarpus())
			);
			panelComponent.getChildren().add(
				createLineComponent("Verzik", plugin.getSplitVerzik())
			);
		}

		// Some characters are wider than others. Replace all of them with "9" in order to prevent shaking of the panel overlay.
		longestString = longestString.replaceAll("([0-8]|[ ]|[-])", "9");

		panelComponent.setPreferredSize(new Dimension(
			graphics.getFontMetrics().stringWidth(longestString) + 6, 0));

		return super.render(graphics);
	}

	private LineComponent createLineComponent(String description, Duration time)
	{
		return createLineComponent(description, time, Color.WHITE);
	}

	private LineComponent createLineComponent(String description, Duration time, Color color)
	{
		LineComponent.LineComponentBuilder builder = LineComponent.builder();
		String timeString = createDurationString(time);

		if (config.showTimeDescription())
		{
			description = " " + description;

			builder
				.left(timeString)
				.leftColor(color)
				.right(description);

			setLongestString(timeString + description);
		}
		else
		{
			builder
				.left(timeString)
				.leftColor(color);

			setLongestString(timeString);
		}

		return builder.build();
	}

	private void setLongestString(String string)
	{
		if (longestString == null || string.length() > longestString.length())
		{
			longestString = string;
		}
	}

	private static String createDurationString(Duration duration)
	{
		if (duration == null)
		{
			return "--:--.-";
		}

		long hours = Math.abs(duration.getSeconds() / 3600);
		long minutes = Math.abs((duration.getSeconds() % 3600) / 60);
		long seconds = Math.abs(duration.getSeconds() % 60);
		long millis = Math.abs(duration.toMillis() - (duration.getSeconds() * 1000)) / 100;

		return (duration.isNegative() ? "-" : "") + (hours > 0
			? String.format("%1d:%02d:%02d.%1d", hours, minutes, seconds, millis)
			: String.format("%02d:%02d.%1d", minutes, seconds, millis));
	}
}
