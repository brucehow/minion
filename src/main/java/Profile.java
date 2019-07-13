import java.sql.ResultSet;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Handles player profiles
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.0
 */
public class Profile extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        User user = event.getAuthor();
        Member member = event.getMember();
        Message msg = event.getMessage();
        String content = msg.getContentRaw();
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();

        if (content.equalsIgnoreCase(".profile") && member.getRoles().contains(Constants.getMemberRole(guild))) {
            String discord = user.getName() + "#" + user.getDiscriminator();
            
            if (!Database.checkDiscordExists(discord)) {
                Main.output("Failed to fetch profile query for " + discord);
                channel.sendMessage(Embed.errorEmbed("Unknown Profile", "Sorry your discord tag did not match any profile :(" +
                        "\nPlease contact a " + Constants.getDevRole(guild).getAsMention() + " if you require more info")).queue();
                return;
            }

            ResultSet result = Database.getProfileFromDiscord(discord);
            channel.sendMessage(Embed.getProfileEmbed(result)).queue();
            return;
        } else if (content.equalsIgnoreCase(".profile help")) {
            channel.sendMessage(Embed.errorEmbed("Invalid Profile Command", "Please view the command usage below\n\n`.profile <discord>` – view a user's profile by discord"
            + "\n`.profile <summoner>` - view a user's profile by summoner name\n`.profile` - view your profile\n\nFor example `.profile Bruce#3218`")).queue();
            return;
        } else if (content.startsWith(".profile") && member.getRoles().contains(Constants.getMemberRole(guild))) {
            if (content.split(" ").length == 1) {
                Main.output("Invalid .profile command from " + user);
                channel.sendMessage(Embed.errorEmbed("Invalid Profile Command", "Please view the command usage below\n\n`.profile <discord>` – view a user's profile by discord"
                + "\n`.profile <summoner>` - view a user's profile by summoner name\n`.profile` - view your profile\n\nFor example `.profile Bruce#3218`")).queue();
                return;
            }
            String discord = content.substring(9);
            if (!discord.contains("#")) {
                discord = Database.getDiscordFromSummoner(discord);
                if (discord == null) {
                    Main.output("Failed to fetch profile query for summoner name " + discord);
                    channel.sendMessage(Embed.errorEmbed("Unknown Profile", "Sorry that summoner name did not match any profiles :(\nPlease view the command usage below\n\n`.profile <discord>` – view a user's profile by discord"
                    + "\n`.profile <summoner>` - view a user's profile by summoner name\n`.profile` - view your profile\n\nFor example `.profile Bruce#3218`")).queue();
                    return;
                }
            }

            if (!Database.checkDiscordExists(discord)) {
                Main.output("Failed to fetch profile query for " + discord);
                channel.sendMessage(Embed.errorEmbed("Unknown Profile", "Sorry that discord tag did not match any profiles :(\nPlease view the command usage below\n\n`.profile <discord>` – view a user's profile by discord"
                    + "\n`.profile <summoner>` - view a user's profile by summoner name\n`.profilec` - view your profile\n\nFor example `.profile Bruce#3218`")).queue();
                    return;
            }
            ResultSet result = Database.getProfileFromDiscord(discord);
            channel.sendMessage(Embed.getProfileEmbed(result)).queue();
            return;
        }
    }

}