import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Handles custom game TFT lobby creation and team shuffling
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.34
 */
public class TFT extends ListenerAdapter {

    private static ArrayList<User> lobbyPlayers;
    private static String priorityPlayers;
    private Message lobbyPost;
    private TextChannel post;
    private Embed tftlobby;

    public static String scorelog = "";
    

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

        if (content.equals(".tft end")) {
            if (member.getRoles().contains(Constants.getModRole(guild)) || Constants.isWhitelist(user)) {
                channel.sendMessage(Embed.errorEmbed("TFT Lobby Ended", "The lobby has been forcibly closed")).queue();
                endLobby();
                return;
            }
        }
        if (content.startsWith(".tft start")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid tft command by " + user + " - Missing Moderator role");
                return;
            }

            post = guild.getTextChannelById(Constants.getLobbyPostID());
            lobbyPlayers = new ArrayList<User>();

            // TFT set variable
            String title = "TFTacticians - Lobby Signup ";
            String desc = "Be the last person standing in a round-based strategy game that pits you against"
            + " seven opponents in a free-for-all race to build a powerful team that fights on your behalf.";
            priorityPlayers = null;

            // Check for priorities
            String[] info = content.split(" ");
            if (info.length >= 3) {
                priorityPlayers = content.substring(11); 
            }

            tftlobby = new Embed(title, desc);

            // Consumer used without lambda - Consumer required for lobbyPost = message;
            post.sendMessage(guild.getPublicRole().getAsMention()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    post.deleteMessageById(message.getId()).queue();
                }
            });
            post.sendMessage(tftlobby.getTFTLobbyEmbed(event.getGuild(), priorityPlayers)).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    Main.output("TFT Lobby message ID (" + message.getId() + ")");
                    lobbyPost = message;
                    Guild emoteGuild = Main.jda.getGuildById(Constants.EMOTES_SERVER);
                    Emote tftemote = emoteGuild.getEmotesByName("TFTacticiansParticipant", true).get(0);
                    post.addReactionById(lobbyPost.getId(), tftemote).queue();
                }
            });
            if (priorityPlayers != null) {
                channel.sendMessage(Embed.successEmbed("TFT Lobby Created", "Successfully created a lobby in the " + 
                post.getAsMention() + " channel\n\nThe following players have been given priority\n`" + priorityPlayers + "`")).queue();
                Main.output("New TFT Lobby with priority (" + priorityPlayers + ") by " + user);
            } else {
                channel.sendMessage(Embed.successEmbed("TFT Lobby Created", "Successfully created a lobby in the " + 
                post.getAsMention() + " channel")).queue();
                Main.output("New TFT Lobby created by " + user);
            }
        }
        else if (content.startsWith(".tft")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid tft command by " + user + " - Missing Moderator role");
                return;
            }
            Main.output("Invalid .tft command without a clause by " + user);
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below.\n\n`.tft start <priorities>\n.tft end`\n\nFor example `.tft start Bruce#3218 don#1234`")).queue();
                return;
        }
        else if (content.startsWith(".score") && member.getRoles().contains(Constants.getModRole(guild))) {
            String[] users = content.split("\n");
            if (users.length == 9) {
                scorelog = "";
                String result = Database.addTFTPoints(users); // summoner names
                if (!result.equals("SUCCESS")) {
                    Main.output(result);
                    channel.sendMessage(Embed.errorEmbed("Oh no!", "Something went wrong, please check the logs or view the summary below\n\n`" + result + "`")).queue();;
                    return;
                }
                channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully updated the values for the appropriate users.\nPlease confirm the update summary below\n\n`"
                + scorelog + "`")).queue();
            } else {
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Parameters", "Please view the command usage below.\n\n`.score`\n`<rank1_summoner>`\n`<rank2_summoner>`\n`...`\n`<rank7_summoner>`\n`<rank8_summoner>`\n")).queue();
                return;
            }
        }
        else if (content.startsWith(".missed") && member.getRoles().contains(Constants.getModRole(guild))) {
            String[] users = content.split("\n");
            if (users.length > 1) {
                scorelog = "";
                String result = Database.addTFTMissedPoints(users); // summoner names
                if (!result.equals("SUCCESS")) {
                    Main.output(result);
                    channel.sendMessage(Embed.errorEmbed("Oh no!", "Something went wrong, please check the logs or view the summary below\n\n`" + result + "`")).queue();;
                    return;
                }
                channel.sendMessage(Embed.successEmbed("Points Increased", "Successfully updated the values for the appropriate users.\nPlease confirm the update summary below\n\n`"
                + scorelog + "`")).queue();
            } else {
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Parameters", "Please view the command usage below.\n\n`.missed `\n`<summoner>`\n`<summoner>`\n`...`\n`<summoner>`\n`<summoner>`\n")).queue();
                return;
            }
        }
    }

    /**
     * Handles TFT lobby registration through reaction
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
        String discordID = user.getId();
        if (!Database.checkDiscordIDExists(discordID)) {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Registration Failed", Constants.registrationFailDiscord(discordID))).queue();
            });
            return;
        }

        if (!lobbyPlayers.contains(event.getUser())) {
            lobbyPlayers.add(event.getUser());
            Main.output("New lobby registration by user " + event.getUser());

            lobbyPost.editMessage(tftlobby.getTFTLobbyEmbed(event.getGuild(), priorityPlayers)).queue();
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

            lobbyPost.editMessage(tftlobby.getTFTLobbyEmbed(event.getGuild(), priorityPlayers)).queue();
        }
    }

    /**
     * Closes the current tft lobby and shuffles the players
     * Can be called manually via '.tft end'
     */
    public void endLobby() {
        post.deleteMessageById(lobbyPost.getId()).queue(); // Delete lobby post
        lobbyPost = null; // Ends the lobby

        // Sends an @everyone message and deletes it
        post.sendMessage(post.getGuild().getPublicRole().getAsMention()).queue(new Consumer<Message>() {
            public void accept(Message message) {
                post.deleteMessageById(message.getId()).queue();
            }
        });
        post.sendMessage(tftlobby.getTFTTeams(getLobbyPlayers(), priorityPlayers)).queue();
    }

    public static ArrayList<User> getLobbyPlayers() {
        return lobbyPlayers;
    }
}

