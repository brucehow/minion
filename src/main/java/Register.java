import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.text.WordUtils;

import java.util.List;

/**
 * Handles guild member registrations
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.5
 */
public class Register extends ListenerAdapter {

    /**
     * Register command for registering new members
     * @param event Register command input
     */
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

        if (content.startsWith(".register")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid register command by " + user + " - Missing Moderator role");
                return;
            }

            String[] info = content.split("\n");
            if (info.length != 5) {
                Main.output("Invalid register command with " + (info.length - 1) + " args instead of 4 by " + user);

                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                        "\n\n`.register\nFull Name\nStudent ID\nSummoner Name\nDiscord ID`")).queue();
                return;
            }
            String name = WordUtils.capitalizeFully(info[1].trim());
            String studentID = info[2].trim();
            String summonerName = info[3].trim();
            String discord = info[4].trim();
            Main.output("Registering new member {" + name + ", " + studentID + ", " + summonerName + ", " + discord + "} by " + user);
            String result = Main.db.addUser(studentID, name, discord, summonerName);
            if (result.equals("SUCCESS")) {
                channel.sendMessage(Embed.successEmbed("Registration Complete",
                        "Successfully registered new member\n\n`" + studentID + ", "+ name + ", "+ discord + ", "+ summonerName + "`")).queue();
            } else {
                channel.sendMessage(Embed.errorEmbed("Registration Failed",
                        "There is an issue with the above registration" +
                                "\n\n`" + result + "`")).queue();
                return;
            }

            GuildController controller = guild.getController();
            String[] id = discord.split("#");
            List<Member> memberList = guild.getMembersByName(id[0], true);
            for (Member m : memberList) {
                if (m.getUser().getDiscriminator().equals(id[1])) {
                    Main.output("Assigning Member role to " + m.getUser());
                    controller.addSingleRoleToMember(m, Constants.getMemberRole(guild)).complete();
                }
            }


        }
    }
}

