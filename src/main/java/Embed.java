import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.ArrayList;

/**
 * Generate MessageEmbed objects based on particular events.
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.1
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
    public MessageEmbed getTFTLobbyEmbed(Guild guild) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#f7ff68"));
        if (TFT.getLobbyPlayers().size() == 0) {
            suffix = "`0`\n`None`\n\n" + guild.getPublicRole().getAsMention() +" React to this post to automatically signup";
            eb.addField("**" + title + "**", prefix+suffix, true);
        } else {
            suffix = "`" + TFT.getLobbyPlayers().size() + "`\n`";
            for (User user : TFT.getLobbyPlayers()) {
                suffix += "\n" + user.getName() + "#" + user.getDiscriminator();
            }
            suffix += "`\n\n" + guild.getPublicRole().getAsMention() +" React to this post to automatically signup";
            eb.addField("**" + title + "**", prefix+suffix, true);
        }
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
}
