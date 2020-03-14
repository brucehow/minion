import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Leaderboard extends ListenerAdapter {

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


        if (content.equals(".riftchampions") || content.equals(".leaderboard") ) {
            if (!member.getRoles().contains(Constants.getMemberRole(guild))) {
                channel.sendMessage(Embed.errorEmbed("Sorry!", "You must be an active UWALC member to use this command :(")).queue();;
                return;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.addField("**Rift Champions " + Constants.year + "**", "\nCurrent standings for Rift Champions are as follows:\n\u200e", false);
            eb.setColor(Color.decode("#609af7"));

            ResultSet result = Database.getRiftChampionLeaderboard();
            Main.output("Leaderboard fetch query received by user " + user);
            int count = 1;
            String countDisplay = "";
            String standings = "";

            try {
                while(result.next()) {
                    Emote icon = guild.getEmotesByName("iron", false).get(0);
                    String points = result.getString("points");
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
                    if (count < 10) {
                        countDisplay = "0" + count;
                    } else {
                        countDisplay = "10";
                    }
                    standings += "**`" + countDisplay + ".`** " + icon.getAsMention() + " **`" + points + " pts`**  " + result.getString("summoner") + "\n";
                    count++;
                }
                    
                eb.addField("**Top 20 Ranked Members**", standings, true);

                result = Database.getRiftChampionLeaderboardPage2();
                count = 1;
                countDisplay = "";
                standings = "";
                
                while(result.next()) {
                    Emote icon = guild.getEmotesByName("iron", false).get(0);
                    String points = result.getString("points");
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
                    if (count < 10) {
                        countDisplay = "1" + count;
                    } else {
                        countDisplay = "20";
                    }
                    standings += "**`" + countDisplay + ".`** " + icon.getAsMention() + " **`" + points + " pts`**  " + result.getString("summoner") + "\n";
                    count++;
                }
            } catch (SQLException e) {
                Main.output(e.toString());
            }
            eb.addField("\u200e", standings, true);
            channel.sendMessage(eb.build()).queue();
        }
    }
}