package com.anonymouslyfast.lunarDiscord.utils;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;

import java.io.Serializable;

public class LogAppender extends AbstractAppender {

    public LogAppender() {
        super("LunarDiscordAppender", null, null, false, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        String channelID = LunarDiscord.getInstance().getConfig().getString("minecraft-logs-channel-id");
        if (channelID == null) channelID = "";
        TextChannel channel = LunarDiscord.getInstance().getJda().getTextChannelById(channelID);
        if (channel == null) {
            LunarDiscord.getInstance().getLogger().warning("Tried logging a message to discord, but the channel ID in config is not set! No message has been sent.");
            return;
        }
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().toString();
        long epochSeconds = event.getTimeMillis() / 1000;
        String discordTimestamp = "<t:" + epochSeconds + ":R>";

        String formatedMessage = String.format("%s **[%s]**: `%s`", discordTimestamp, level, message);

        channel.sendMessage(formatedMessage).queue();
    }
}
