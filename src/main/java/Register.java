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
 * @version 1.3
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
            if (!member.getRoles().contains(Constants.getCommitteeRole(guild)) && !Constants.isWhitelist(user)) {
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

            // Check for discord ID
            GuildController controller = guild.getController();
            if (!discord.contains("#")) {
                channel.sendMessage(Embed.errorEmbed("Invalid Discord", "Discord is missing its discriminator and tag")).queue();
                return;
            }
            String[] id = discord.split("#");
            List<Member> memberList = guild.getMembersByName(id[0], true);
            Member mem = null;
            String uid = "NULL";
            for (Member m : memberList) {
                if (m.getUser().getDiscriminator().equals(id[1])) {
                    mem = m;
                    uid = mem.getUser().getId();
                    Main.output("Successfully identified user in registration");
                    break;
                }
            }

            // Check for SummonerName
            String encrypted = RiotAPI.getSummonerEncryptedID(summonerName);
            if (encrypted == null) {
                Main.output("Registration failed could not find summoner name " + summonerName);
                channel.sendMessage(Embed.errorEmbed("Registration Failed", "The summoner name `" + summonerName + "` does not exist")).queue();
                return;
            }

            if (uid.equals("NULL")) {
                Main.output("Registration failed could not find discord ID " + discord);
                channel.sendMessage(Embed.errorEmbed("Registration Failed", "The discord tag `" + discord + "` does not exist")).queue();
                return;
            }
            String res = Database.extendRegistration(studentID, discord, uid);
            if (res.equals("EXTEND")) {
                Main.output("Extended user registration {" + studentID + ", " + uid + "}");
                channel.sendMessage(Embed.successEmbed("Registration Extended", "Existing user's registration has been extended to " + Constants.membership + 
                "\n`" + studentID + ", " + discord + ", " + "U:" + uid + "`")).queue();
                Main.output("Assigning Member role to " + mem.getUser());
                controller.addSingleRoleToMember(mem, Constants.getMemberRole(guild)).complete();
                controller.removeSingleRoleFromMember(mem, Constants.getGuestRole(guild)).complete();
                return;
            } else if (res.equals("FAILED")) {
                Main.output("Failed user extension {" + studentID + ", " + uid + "}");
                channel.sendMessage(Embed.errorEmbed("Registration Extension Failed", "User exists, but could not extend registration")).queue();
                return;
            }

            if (studentID.equals("?")) {
                studentID = String.valueOf(Database.getNextStudentID());
                Main.output("Non UWA student assigned new student ID " + studentID);
            }

            Main.output("Registering new member {" + name + ", " + studentID + ", " + summonerName + ", " + discord + ", " + uid + ", " + encrypted + "} by " + user);
            String result = Database.addUser(studentID, name, discord, summonerName, uid, encrypted);
            if (result.equals("SUCCESS")) {
                channel.sendMessage(Embed.successEmbed("Registration Complete",
                        "Successfully registered new member\n\n`" + studentID + ", "+ name + ", "+ discord + ", "+ summonerName + "`\n`U:" + uid + "`"
                         + "\n\nUser has been registered until `" + Constants.membership + "`")).queue();
            } else {
                channel.sendMessage(Embed.errorEmbed("Registration Failed",
                        "There is an issue with the above registration" +
                                "\n\n`" + result + "`")).queue();
                return;
            }
            Main.output("Assigning Member role to " + mem.getUser());
            controller.addSingleRoleToMember(mem, Constants.getMemberRole(guild)).complete();
            controller.removeSingleRoleFromMember(mem, Constants.getGuestRole(guild)).complete();
        }
    }
}

