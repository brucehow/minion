import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Handles custom game lobby creation and team shuffling
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.5
 */
public class Lobby extends ListenerAdapter {

    private static ArrayList<User> lobbyPlayers;
    private Message lobbyPost;
    private TextChannel general;
    private Embed lobby;
    private String title;

    /**
     * Handles the lobby commands and its sub-commands
     * @param event Message received event
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

        if (content.equals(".lobby end")) {
            if (member.getRoles().contains(Constants.getModRole(guild)) || Constants.isWhitelist(user)) {
                channel.sendMessage(Embed.errorEmbed("Lobby Ended", "The lobby has been forcibly closed")).queue();
                endLobby();
                return;
            }
        }
        if (content.startsWith(".lobby")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid lobby command by " + user + " - Missing Moderator role");
                return;
            }

            general = guild.getTextChannelById(Constants.getLobbyPostID());

            String[] info = content.split("\n");
            if (info.length != 3) {
                Main.output("Invalid lobby command with " + (info.length - 1) + " args instead of 2 by " + user);
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", 
                    "Please view the command usage below\n\n`.lobby\nTitle\nDescription`")).queue();
                return;
            }
            String title = info[1];
            this.title = title;
            String desc = info[2];

            lobbyPlayers = new ArrayList<User>();

            lobby = new Embed(title, desc);

            // Consumer used without lambda - Consumer required for lobbyPost = message;
            general.sendMessage(guild.getPublicRole().getAsMention()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    general.deleteMessageById(message.getId()).queue();
                }
            });
            general.sendMessage(lobby.getLobbyEmbed(event.getGuild())).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    Main.output("Lobby message ID (" + message.getId() + ")");
                    lobbyPost = message;
                    general.addReactionById(lobbyPost.getId(), "\uD83C\uDFC6").queue();
                }
            });
            channel.sendMessage(Embed.successEmbed("Lobby Created", "Successfully created a lobby in the " + 
                general.getAsMention() + " channel")).queue();
            Main.output("New lobby created {" + title + ", " + desc + "} by " + user);
        }
    }

    /**
     * Handles lobby registration through reaction
     * @param event Reaction add event
     */
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (lobbyPost == null || event.getUser().isBot() || !event.getMessageId().equals(lobbyPost.getId())) {
            return;
        }
        User user = event.getUser();
        Guild guild = event.getGuild();

        // Checking for Member role
        if (!event.getMember().getRoles().contains(Constants.getMemberRole(event.getGuild()))) {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Registration Failed", Constants.registrationFailRole(guild))).queue();
            });
            return;
        }

        // Checking for valid discord tag
        String discordID = user.getName() + "#" + user.getDiscriminator();
        if (!Database.checkDiscordExists(discordID)) {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Registration Failed", Constants.registrationFailDiscord(discordID))).queue();
            });
            return;
        }

        if (lobbyPlayers.size() == 10) {
            return; // Incase two reaction come in asynchronously
        }
        if (!lobbyPlayers.contains(event.getUser())) {
            lobbyPlayers.add(event.getUser());
            Main.output("New lobby registration by user " + event.getUser());

            lobbyPost.editMessage(lobby.getLobbyEmbed(event.getGuild())).queue();
            if (lobbyPlayers.size() == 10) {
                endLobby();
            }
        }
    }

    /**
     * Handles lobby registration removal through reaction removal
     * @param event Reaction remove event
     */
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (lobbyPost == null || event.getUser().isBot() || !event.getMessageId().equals(lobbyPost.getId())) {
            return;
        }
        if (lobbyPlayers.contains(event.getUser())) {
            lobbyPlayers.remove(event.getUser());
            Main.output("New lobby registration removal by user " + event.getUser());

            lobbyPost.editMessage(lobby.getLobbyEmbed(event.getGuild())).queue();
        }
    }

    /**
     * Closes the current lobby and shuffles the players
     * Can be called manually via '.lobby end' or when the lobby reaches the player limit
     */
    public void endLobby() {
        general.deleteMessageById(lobbyPost.getId()).queue(); // Delete lobby post
        lobbyPost = null; // Ends the lobby

        Matchmaking mm = new Matchmaking(lobbyPlayers);
        mm.bruceMM();
        ArrayList<String> team1 = mm.getTeam1();
        ArrayList<String> team2 = mm.getTeam2();

        // Sends an @everyone message and deletes it
        general.sendMessage(general.getGuild().getPublicRole().getAsMention()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    general.deleteMessageById(message.getId()).queue();
                }
            });

        general.sendMessage(lobby.getFullLobbyList(team1, team2, mm.getTeam1MMR(), mm.getTeam2MMR())).queue();
        lobbyPlayers = null;

        // Admin handling
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("**" + title + "**", "Please run **.score win/lose** and **.score mvp** followed by the team of" +
                " players.\nPlease include only one summoner name on each line. Teams are listed below\n", false);

        String t1 = "Team 1\n";
        String t2 = "Team 2\n";
        for (String disc : team1) {
            t1 += disc + "\n";
        }
        for (String disc : team2) {
            t2 += disc + "\n";
        }

        eb.addField("\u200e", t1, true);
        eb.addField("\u200e", t2, true);
        general.getGuild().getTextChannelById(Constants.getPointsID()).sendMessage(eb.build()).queue();
    }

    public static ArrayList<User> getLobbyPlayers() {
        return lobbyPlayers;
    }

}

