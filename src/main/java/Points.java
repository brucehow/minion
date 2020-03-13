import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;

/**
 * Handles point allocation
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.0
 */
public class Points extends ListenerAdapter {

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

        if (content.startsWith(".score")) {
            if (!member.getRoles().contains(Constants.getCommitteeRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid points command by " + user + " - Missing Moderator role");
                return;
            }

            String[] info = content.split("\n");
            if (info[0].equals(".score")) {
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the commands usage below" +
                        "\n\n`.score win\n.score lose\n.score mvp\n.score ace`")).queue();
                return;
            }
            if (info.length != 6 && !info[0].startsWith(".score mvp") && !info[0].startsWith(".score ace")) {
                Main.output("Invalid points command with " + (info.length - 1) + " args instead of 5 by " + user);

                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                        "\n\n`.score win/lose\nPlayer 1\nPlayer 2\nPlayer 3\nPlayer 4\nPlayer 5\n`")).queue();
                return;
            }

            // Winning team
            if (content.startsWith(".score win")) {
                ArrayList<String> players = new ArrayList<String>();
                String playerList = "";
                for (int i = 1; i < 6; i++) {
                    playerList += info[i] + " ";
                    players.add(info[i]);
                }
                String result = Database.addPoints(players, 20, true);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 20 points to the " +
                        "following players\n\n`" + playerList + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            }
            // Losing team
            else if (content.startsWith(".score lose")) {
                ArrayList<String> players = new ArrayList<String>();
                String playerList = "";
                for (int i = 1; i < 6; i++) {
                    playerList += info[i] + " ";
                    players.add(info[i]);
                }
                String result = Database.addPoints(players, 10, false);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 10 points to the " +
                        "following players\n\n`" + playerList + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            } else if(content.startsWith(".score mvp")) {
                if (!member.getRoles().contains(Constants.getCommitteeRole(guild)) && !Constants.isWhitelist(user)) {
                    Main.output("Invalid points command by " + user + " - Missing Moderator role");
                    return;
                }
                if (info.length != 2) {
                    Main.output("Invalid points command with " + (info.length - 1) + " args instead of 1 by " + user);

                    // Error Message
                    channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                            "\n\n`.score mvp\nPlayer 1`")).queue();
                    return;
                }
                String result = Database.addMVPPoints(info[1], true);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 5 points to the " +
                            "following player\n\n`" + info[1] + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            } else if(content.startsWith(".score ace")) {
                if (!member.getRoles().contains(Constants.getCommitteeRole(guild)) && !Constants.isWhitelist(user)) {
                    Main.output("Invalid points command by " + user + " - Missing Moderator role");
                    return;
                }
                if (info.length != 2) {
                    Main.output("Invalid points command with " + (info.length - 1) + " args instead of 1 by " + user);

                    // Error Message
                    channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                            "\n\n`.score mvp\nPlayer 1`")).queue();
                    return;
                }
                String result = Database.addMVPPoints(info[1], false);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 5 points to the " +
                            "following player\n\n`" + info[1] + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            }
        }

    }
}
