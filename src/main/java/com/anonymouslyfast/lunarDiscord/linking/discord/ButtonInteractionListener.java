package com.anonymouslyfast.lunarDiscord.linking.discord;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;

public class ButtonInteractionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("linkingButton")) return;

        TextInput MinecraftUsername = TextInput.create("minecraft_username", TextInputStyle.SHORT)
                .setPlaceholder("Your Minecraft Username")
                .setRequiredRange(3, 16)
                .setRequired(true)
                .build();

        TextInput code = TextInput.create("one_time_code", TextInputStyle.SHORT)
                .setPlaceholder("One time code from /link")
                .setRequired(true)
                .build();

        Modal modal = Modal.create("linkingModal", "Link Your Accounts!")
                .addComponents(Label.of("Minecraft Username", MinecraftUsername), Label.of("One Time Code", code))
                .build();

        event.replyModal(modal).queue();
    }

}
