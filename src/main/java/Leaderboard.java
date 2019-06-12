import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Leaderboard extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        User user = event.getAuthor();
        Message msg = event.getMessage();
        String content = msg.getContentRaw();
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();


        if (content.equals(".leaderboard")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.addField("**Rift Champions**", "\nCurrent standings for **Rift Champions** are as follows:\n\u200e", false);
            eb.setColor(Color.decode("#609af7"));

            ResultSet result = Database.getRiftChampionLeaderboard();
            Main.output("Leaderboard fetch query received by user " + user);
            int count = 1;
            String countDisplay = "";
            String standings = "";
            try {
                while(result.next()) {
                    Emote icon = guild.getEmotesByName("iron", false).get(0);
                    String[] id = result.getString("discord").split("#");
                    List<Member> memberList = guild.getMembersByName(id[0], true);
                    String points = result.getString("points");
                    User usr = null;
                    for (Member m : memberList) {
                        if (m.getUser().getDiscriminator().equals(id[1])) {
                            usr = m.getUser();
                        }
                    }
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
                    if (usr == null) {
                        standings += "**`" + countDisplay + ".`** " + icon.getAsMention() + " **`" + points + " pts`** " +
                                id[0] + "#" + id[1] + " - " + result.getString("summoner") + "\n";
                    } else {
                        standings += "**`" + countDisplay + ".`** " + icon.getAsMention() + " **`" + points + " pts`** " + 
                            usr.getAsMention() + " - " + result.getString("summoner") + "\n";
                    }
                    count++;
                }
            } catch (SQLException e) {
                Main.output(e.toString()); // Should no longer happen with scheduled RDS restarts
            }
            eb.addField("**Top Ranked Members**", standings, false);
            channel.sendMessage(eb.build()).queue();
        }
    }
}
