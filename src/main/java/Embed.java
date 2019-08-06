import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Generate MessageEmbed objects based on particular events.
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.34
 */
public class Embed {

    // Handles Lobby Embeds
    private String title;
    private String desc;
    private String prefix;
    private String suffix;

    /**
     * Create an error MessageEmbed
     * @param title Title for the embed
     * @param desc Description for the embed
     * @return The MessageEmbed object
     */
    public static MessageEmbed errorEmbed(String title, String desc) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("**" + title + "**", desc, true);
        eb.setColor(Color.decode("#DD4B5F"));
        return eb.build();
    }

    /**
     * Create a success MessageEmbed
     * @param title Title for the embed
     * @param desc Description for the embed
     * @return The MessageEmbed object
     */
    public static MessageEmbed successEmbed(String title, String desc) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("**" + title + "**", desc, true);
        eb.setColor(Color.decode("#77ab59"));
        return eb.build();
    }

    /**
     * Create a neutral MessageEmbed
     * @param title Title for the embed
     * @param desc Description for the embed
     * @return The MessageEmbed object
     */
    public static MessageEmbed neutralEmbed(String title, String desc) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("**" + title + "**", desc, true);
        eb.setColor(Color.decode("#609af7"));
        return eb.build();
    }

    /**
     * Constructor for the integration with Lobbies
     * @param title Title for the lobby
     * @param desc Description for the lobby
     */
    public Embed(String title, String desc) {
        this.title = title;
        this.desc = desc;
        prefix = desc + "\n\nRegistered players ";
    }

    /**
     * Generates the lobby embed for the particular object
     * @param guild The guild to generate for
     * @return The MessageEmbed object for the open lobby
     */
    public MessageEmbed getLobbyEmbed(Guild guild) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#f7ff68"));
        if (Lobby.getLobbyPlayers().size() == 0) {
            suffix = "`0/10`\n`None`\n\n" + guild.getPublicRole().getAsMention() +" React to this post to automatically signup";
            eb.addField("**" + title + "**", prefix+suffix, true);
        } else {
            suffix = "`" + Lobby.getLobbyPlayers().size() + "/10`\n`";
            for (User user : Lobby.getLobbyPlayers()) {
                suffix += "\n" + user.getName() + "#" + user.getDiscriminator();
            }
            suffix += "`\n\n" + guild.getPublicRole().getAsMention() +" React to this post to automatically signup";
            eb.addField("**" + title + "**", prefix+suffix, true);
        }
        return eb.build();
    }

    /**
     * Generates the TFT lobby embed for the particular object
     * @param guild The guild to generate for
     * @return The MessageEmbed object for the open lobby
     */
    public MessageEmbed getTFTLobbyEmbed(Guild guild, String priorityPlayers) {
        Guild emoteGuild = Main.jda.getGuildById(Constants.EMOTES_SERVER);
        Emote tftemote = emoteGuild.getEmotesByName("TFTacticiansParticipant", true).get(0);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#f7ff68"));
        eb.setFooter("React to this post to automatically signup", null);   
        if (priorityPlayers != null) {
            eb.addField(tftemote.getAsMention() + " **" + title + "**", desc + "\n\nThe following players have been given priorty for this lobby\n`" + priorityPlayers + "`\n", false);
        } else {
            eb.addField(tftemote.getAsMention() + " **" + title + "**", desc + "\n", false);
        }
        if (TFT.getLobbyPlayers().size() == 0) {
            eb.addField("\u200e", "Registered players `0`\n`None`\n\u200e", true);
        } else {
            String playerCount = "`" + TFT.getLobbyPlayers().size() + "`";
            String playersLeft = "";
            String playersMiddle = "";
            String playersRight = "";
            for (int i = 0; i < TFT.getLobbyPlayers().size(); i++) {
                User user = TFT.getLobbyPlayers().get(i);
                if (i % 3 == 0) {
                    playersLeft += "\n`" + user.getName() + "#" + user.getDiscriminator() + "`";
                } else if (i % 3 == 1) {
                    playersMiddle += "\n`" + user.getName() + "#" + user.getDiscriminator() + "`";
                } else {
                    playersRight += "\n`" + user.getName() + "#" + user.getDiscriminator() + "`";
                } 
            }
            eb.addField("\u200e", "Registered players " + playerCount + playersLeft + "\n\u200e", true);
            eb.addField("\u200e", "\u200e" + playersMiddle, true);
            eb.addField("\u200e", "\u200e" + playersRight, true);
        }
        return eb.build();
    }

    /**
     * Generates the finalised TFT lobby embed for a particular user list
     * @return The MessageEmbed object for the lobby
     */
    public MessageEmbed getTFTTeams(ArrayList<User> users, String priorityPlayers) {
        Guild emoteGuild = Main.jda.getGuildById(Constants.EMOTES_SERVER);
        Emote tftemote = emoteGuild.getEmotesByName("TFTacticiansParticipant", true).get(0);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#f7ff68"));
        Collections.shuffle(users);
        if (priorityPlayers != null) {
            Main.output("Detected priority players (" + priorityPlayers + ")");
            int swapPos = 0;
            for (int i = 0; i < users.size(); i++) {
                String discord = users.get(i).getName() + "#" + users.get(i).getDiscriminator();
                if (priorityPlayers.contains(discord)) {
                    Main.output("Swapped " + users.get(i) + " to the position " + swapPos); 
                    Collections.swap(users, i, swapPos);
                    swapPos++;
                }
            }
        }
        eb.addField(tftemote.getAsMention() + " **" + title + "**", desc + "\n\nPlayers have been drafted into groups of 8 based on random shuffling\n\u200e", false);
        int teamCount = users.size()/8;
        char[] groups = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L'};
        
        for (int i = 0; i < teamCount; i++) {
            String group = "";
            for (int j = (8*i); j < (8*i) + 8; j++) {
                User user = users.get(j);
                String discord = user.getName() + "#" + user.getDiscriminator();
                group += "`" + discord + " (" + Database.getSummonerFromDiscord(discord) + ")`\n";
            }
            eb.addField("Group " + groups[i], group + "\n\u200e", true);
        }
        // Formating into threes
        if (teamCount % 2 != 0) {
            eb.addBlankField(true);
        }

        // Checking for missed players
        if (users.size() % 8 != 0) {
            String missedPlayers = "";
            for (int i = teamCount * 8; i < users.size(); i++) {
                missedPlayers += "`" + users.get(i).getName() + "#" + users.get(i).getDiscriminator() + "` ";
            }
            eb.addField("\u200e", "The following players were not placed into a group and will been given priority\n" + missedPlayers, false);        }
        return eb.build();
    }


    /**
     * Generates the finalised lobby embed for the particular object
     * @param team1 The first team
     * @param team2 The second team
     * @return The MessageEmbed object for the open lobby
     */
    public MessageEmbed getFullLobbyList(ArrayList<String> team1, ArrayList<String> team2, int team1mmr, int team2mmr) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#f7ff68"));

        String players1 = "";
        String players2 = "";

        for (String discord : team1) {
            players1 += "\n" + discord + " - " + RiotAPI.fetchSummonerInfoString(discord);
        }
        for (String discord : team2) {
            players2 += "\n" + discord + " - " + RiotAPI.fetchSummonerInfoString(discord);
        }

        eb.addField("**" + title + "**", desc + "\n\nPlayers have been drafted into two equal teams based on individual rank\n\u200e", false);

        if (team1.size() == 0 || team2.size() == 0) {
            eb.addField("Team 1 (" + team1mmr + " MMR)", players1, true);
            eb.addField("Team 2 (" + team2mmr + " MMR)", players2, true);
        } else if (team1mmr <= team2mmr) {
            eb.addField("Team 1 (" + team1mmr / team1.size() + " MMR)", players1, true);
            eb.addField("Team 2 (" + team2mmr / team2.size() + " MMR)", players2, true);
        } else {
            eb.addField("Team 1 (" + team2mmr / team2.size() + " MMR)", players2, true);
            eb.addField("Team 2 (" + team1mmr / team1.size() + " MMR)", players1, true);
        }
        return eb.build();
    }

    /**
     * Generates the profile embed for the particular object
     * @param guild The guild to generate for
     * @return The MessageEmbed object for the open lobby
     */
    public static MessageEmbed getProfileEmbed(ResultSet result, ResultSet tftResult) {
        EmbedBuilder eb = new EmbedBuilder();
        String summoner = "Unknown";
        String position = null;
        String join_ts = "00/00/0000";
        String rift_points = "0";
        String rift_rank = "Unranked";
        Guild emoteGuild = Main.jda.getGuildById(Constants.EMOTES_SERVER);
        String[] badges = null;
        int wins = 0;
        int losses = 0;
        int ace = 0;
        int mvp = 0;
        int tftwins = 0;
        int tftpoints = 0;
        String avgrank = "0";
        int topfour = 0;
        int tftgames = 0;
        try {
            result.next();
            tftResult.next();
            badges = result.getString("badges").split(",");
            summoner = result.getString("summoner");
            position = result.getString("position");
            join_ts = result.getString("join_ts").split(" ")[0];
            join_ts = join_ts.substring(8, 10) + "/" + join_ts.substring(5, 7) + "/" + join_ts.substring(0, 4);
            rift_points = result.getString("points");
            if (Integer.parseInt(rift_points) != 0) {
                rift_rank = result.getString("riftrank");
            }
            wins = Integer.parseInt(result.getString("wins"));
            losses = Integer.parseInt(result.getString("losses"));
            mvp = Integer.parseInt(result.getString("mvp"));
            ace = Integer.parseInt(result.getString("ace"));
            tftwins = Integer.parseInt(tftResult.getString("wins"));
            tftpoints = Integer.parseInt(tftResult.getString("points"));
            topfour = Integer.parseInt(tftResult.getString("topfour"));
            avgrank = tftResult.getString("avgrank");
            tftgames = tftResult.getShort("games");
        } catch (Exception e) {
            Main.output("Failed to get badges from user");
            e.printStackTrace();
        }
        if (position == null) {
            position = "";
        } else {
            position = "\n" + position;
        }
        eb.addField("**Member Info**", summoner + position + "\nMember since " + join_ts + "\n\u200e", false);
        if (badges != null && !badges[0].equals("Empty")) {
            String badgeList = "";
            for (String badge : badges) {
                badgeList += emoteGuild.getEmotesByName(badge, true).get(0).getAsMention() + " ";
            }
            eb.setDescription(badgeList + "\n\u200e");
        }

        double winrate = 0;
        String winrateDisplay = "0";
        if (wins != 0) {
            int games = wins + losses;
            winrate = (double) wins/games * 100;
            winrateDisplay = Integer.toString((int) Math.round(winrate)) + "%";
            if ((int) Math.round(winrate) > 60) {
                winrateDisplay = "**" + winrateDisplay + "**";
            }
        }

        if (Integer.parseInt(rift_points) >= 700) {
            rift_points = "**" + rift_points + "**";
        }

        if (rift_rank.endsWith("1") && !rift_rank.endsWith("11")) {
            rift_rank = rift_rank + "st";
        } else if (rift_rank.endsWith("2") && !rift_rank.endsWith("12")) {
            rift_rank = rift_rank + "nd";
        } else if (rift_rank.endsWith("3") && !rift_rank.endsWith("13")) {
            rift_rank = rift_rank + "rd";
        } else if (!rift_rank.equals("Unranked")) {
            rift_rank = rift_rank + "th";
        }

        double avgrankCalc = Double.parseDouble(avgrank);
        avgrankCalc = avgrankCalc / tftgames;
        avgrank = Integer.toString((int) Math.floor(avgrankCalc));
        if (avgrank.endsWith("1") && !avgrank.endsWith("11")) {
            avgrank = "**" + avgrank + "st**";
        } else if (avgrank.endsWith("2") && !avgrank.endsWith("12")) {
            avgrank = "**" + avgrank + "nd**";
        } else if (avgrank.endsWith("3") && !avgrank.endsWith("13")) {
            avgrank = "**" + avgrank + "rd**";
        } else {
            avgrank = avgrank + "th";
        }
        
        if (rift_rank.length() == 3 || rift_rank.equals("10th")) {
            rift_rank = "**" + rift_rank + "**";
        }
        eb.addField("**Rift Champions**", rift_points + " Points (" + rift_rank + ")\n" + wins + "W " + losses + "L " + 
        "(" + winrateDisplay + " WR)\n" + mvp + " MVP " + ace + " ACE\n\u200e", true);
        eb.addField("**Teamfight Tacticians**", tftpoints + " Points\n" + tftwins + " W (" + topfour + " Top 4)\n" + avgrank + " Place Avg", true);
        eb.setFooter("Badges can be obtained through participation and accomplishments", null);
        return eb.build();
    }
}
