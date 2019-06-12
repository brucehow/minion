import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        if (content.equalsIgnoreCase(".points") && member.getRoles().contains(Constants.getMemberRole(guild))) {
            String discord = user.getName() + "#" + user.getDiscriminator();

            ResultSet result = Database.getIndividualPoint(discord);
            if (result == null) {
                Main.output("Failed to fetch point query for " + discord);
                channel.sendMessage(Embed.errorEmbed("Unknown User", "Sorry I could not find your profile :(" +
                        "\nPlease contact a " + Constants.getDevRole(guild).getAsMention() + " for more info")).queue();
                return;
            }
            try {
                result.next();
                String points = result.getString("points");
                Emote icon = guild.getEmotesByName("iron", false).get(0);
                int pointInt = Integer.parseInt(points);
                    if (pointInt >= 700) {
                            icon = guild.getEmotesByName("challenger", false).get(0);
                        } else if (pointInt >= 500) {
                            icon = guild.getEmotesByName("grandmaster", false).get(0);
                        } else if (pointInt >= 300) {
                            icon = guild.getEmotesByName("master", false).get(0);
                        } else if (pointInt >= 220) {
                            icon = guild.getEmotesByName("diamond", false).get(0);
                        } else if (pointInt >= 160) {
                            icon = guild.getEmotesByName("platinum", false).get(0);
                        } else if (pointInt >= 100) {
                            icon = guild.getEmotesByName("gold", false).get(0);
                        } else if (pointInt >= 40) {
                            icon = guild.getEmotesByName("silver", false).get(0);
                        } else if (pointInt >= 20) {
                            icon = guild.getEmotesByName("bronze", false).get(0);
                        }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.addField("**Member Profile**", "Statistics for Rift Champions\n"
                            + icon.getAsMention() + " `" + pointInt + " pts` " +
                            user.getAsMention() + " - " + RiotAPI.fetchSummonerInfoString(discord), false);
                    eb.setColor(Color.decode("#609af7"));
                    channel.sendMessage(eb.build()).queue();
            } catch (SQLException e) {
                Main.output(e.toString());
                channel.sendMessage(Embed.errorEmbed("Unknown User", "Sorry I could not find your profile :(" +
                        "\nFor more info, please contact a " + Constants.getDevRole(guild).getAsMention())).queue();
                return;
            }
        } else if (content.startsWith(".update")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid points command by " + user + " - Missing Moderator role");
                return;
            }

            String[] info = content.split("\n");
            if (info[0].equals(".update")) {
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the commands usage below" +
                        "\n\n`.update win\n.update lose\n.update mvp`")).queue();
                return;
            }
            if (info.length != 6 && !info[0].startsWith(".update mvp")) {
                Main.output("Invalid points command with " + (info.length - 1) + " args instead of 5 by " + user);

                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                        "\n\n`.points win/lose\nPlayer 1\nPlayer 2\nPlayer 3\nPlayer 4\nPlayer 5\n`")).queue();
                return;
            }

            // Winning team
            if (content.startsWith(".update win")) {
                ArrayList<String> players = new ArrayList<String>();
                String playerList = "";
                for (int i = 1; i < 6; i++) {
                    playerList += info[i] + " ";
                    players.add(info[i]);
                }
                String result = Main.db.addPoints(players, 20);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 30 points to the " +
                        "following players\n\n`" + playerList + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            }
            // Losing team
            else if (content.startsWith(".update lose")) {
                ArrayList<String> players = new ArrayList<String>();
                String playerList = "";
                for (int i = 1; i < 6; i++) {
                    playerList += info[i] + " ";
                    players.add(info[i]);
                }
                String result = Main.db.addPoints(players, 10);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 15 points to the " +
                        "following players\n\n`" + playerList + "`")).queue();
                } else {
                    channel.sendMessage(Embed.errorEmbed("Point Allocation Failed",
                            "There is an issue with the above point allocation"
                                    + "\n\n`" + result + "`")).queue();
                    return;
                }
            } else if(content.startsWith(".update mvp")) {
                if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                    Main.output("Invalid points command by " + user + " - Missing Moderator role");
                    return;
                }
                if (info.length != 3) {
                    Main.output("Invalid points command with " + (info.length - 1) + " args instead of 2 by " + user);

                    // Error Message
                    channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                            "\n\n`.update mvp\nPlayer 1\nPlayer 2`")).queue();
                    return;
                }
                // MVP/ACE players
                ArrayList<String> players = new ArrayList<String>();
                String playerList = "";
                for (int i = 1; i < 3; i++) {
                    playerList += info[i] + " ";
                    players.add(info[i]);
                }
                String result = Main.db.addPoints(players, 5);
                if (result.equals("SUCCESS")) {
                    channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully added 5 points to the " +
                            "following players\n\n`" + playerList + "`")).queue();
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
