import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.Random;

/**
 * Responsible for auto assigning roles to users
 * on GuildMemberJoinEvent and MessageReaction events
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.3
 */
public class AutoRole extends ListenerAdapter {

    /**
     * Outputs when a new member joins the server and automatically 
     * applies the Member role if the discriminator exists in MySQL
     * @param event Guild member join event
     */
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String fullDiscordID = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
        Main.output("User " + fullDiscordID + " joined the server");

        if (Database.checkDiscordExists(fullDiscordID)) {
            event.getGuild().getTextChannelById(Constants.getCommitteeID())
                    .sendMessage(Embed.successEmbed("Member Registration Required",
                            event.getUser().getAsMention() + " has joined the server and requires the **Member** role")).queue();
            Main.output(event.getUser() + " joined the server and requires the Member role");
        } else {
            int random = new Random().nextInt(Constants.header.length);
            Guild guild = event.getGuild();
            User user = event.getUser();
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.neutralEmbed(Constants.header[random] + " :wave:", Constants.welcomeMsg(user, guild)));
            });
            return;
        }
    }

    /**
     * Automatically assigns the respective discord role to users.
     * Handles both Positional roles and the Looking For Game role
     * @param event Message reaction received event
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        MessageChannel channel = event.getChannel();

        // Looking For Game role
        if (event.getMessageId().equals(Constants.getLFGMsgID())) {
            Member member = event.getMember();
            User user = event.getUser();
            Guild guild = event.getGuild();
            GuildController controller = guild.getController();

            controller.addSingleRoleToMember(member, Constants.getLFGRole(guild)).complete();
            Main.output("Auto assigned the Looking For Game role to " + user);

            return;
        }

        // Positional roles
        if (channel.getId().equals(Constants.getClubRulesID())) {
            Member member = event.getMember();
            User user = event.getUser();
            String reaction = event.getReaction().getReactionEmote().getName();
            Guild guild = event.getGuild();
            GuildController controller = guild.getController();

            if (reaction.equals("toplane")) {
                controller.addSingleRoleToMember(member, Constants.getTopRole(guild)).complete();
                Main.output("Auto assigned the Top role to " + user);

            } else if (reaction.equals("bottom")) {
                controller.addSingleRoleToMember(member, Constants.getBotRole(guild)).complete();
                Main.output("Auto assigned the Bottom role to " + user);

            } else if (reaction.equals("middle")) {
                controller.addSingleRoleToMember(member, Constants.getMidRole(guild)).complete();
                Main.output("Auto assigned the Middle role to " + user);

            } else if (reaction.equals("support")) {
                controller.addSingleRoleToMember(member, Constants.getSupRole(guild)).complete();
                Main.output("Auto assigned the Support role to " + user);

            } else if (reaction.equals("jungle")) {
                controller.addSingleRoleToMember(member, Constants.getJungleRole(guild)).complete();
                Main.output("Auto assigned the Jungle role to " + user);

            } else if (reaction.equals("fill")) {
                controller.addSingleRoleToMember(member, Constants.getFillRole(guild)).complete();
                Main.output("Auto assigned the Fill role to " + user);

            } else {
                Main.output("Reaction for an invalid role (" + reaction + " by " + user + ")");
            }
        }
    }

    /**
     * Automatically un-assigns the respective discord role to users.
     * Handles both Positional roles and the Looking For Game role
     * @param event Message reaction received event
     */
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        MessageChannel channel = event.getChannel();

        // "Looking For Game" role
        if (event.getMessageId().equals(Constants.getLFGMsgID())) {
            Member member = event.getMember();
            User user = event.getUser();
            Guild guild = event.getGuild();
            GuildController controller = guild.getController();

            controller.removeSingleRoleFromMember(member, Constants.getLFGRole(guild)).complete();
            Main.output("Auto removed the Looking For Game role from " + user);
            return;
        }

        // Positional roles
        if (channel.getId().equals(Constants.getClubRulesID())) {
            Member member = event.getMember();
            User user = event.getUser();
            String reaction = event.getReaction().getReactionEmote().getName();
            Guild guild = event.getGuild();
            GuildController controller = guild.getController();

            if (reaction.equals("toplane")) {
                controller.removeSingleRoleFromMember(member, Constants.getTopRole(guild)).complete();
                Main.output("Auto removed the Top role from " + user);

            } else if (reaction.equals("bottom")) {
                controller.removeSingleRoleFromMember(member, Constants.getBotRole(guild)).complete();
                Main.output("Auto removed the Bottom role from " + user);

            } else if (reaction.equals("middle")) {
                controller.removeSingleRoleFromMember(member, Constants.getMidRole(guild)).complete();
                Main.output("Auto removed the Middle role from " + user);

            } else if (reaction.equals("support")) {
                controller.removeSingleRoleFromMember(member, Constants.getSupRole(guild)).complete();
                Main.output("Auto removed the Support role from " + user);

            } else if (reaction.equals("jungle")) {
                controller.removeSingleRoleFromMember(member, Constants.getJungleRole(guild)).complete();
                Main.output("Auto removed the Jungle role from " + user);

            } else if (reaction.equals("fill")) {
                controller.removeSingleRoleFromMember(member, Constants.getFillRole(guild)).complete();
                Main.output("Auto removed the Fill role from " + user);

            } else {
                Main.output("Reaction for an invalid role (" + reaction + " by " + user + ")");
            }
        }
    }
}
