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
 * @version 1.5
 */
public class TFT extends ListenerAdapter {

    private static ArrayList<User> lobbyPlayers;
    private Message lobbyPost;
    private TextChannel post;
    private Embed tftlobby;

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

            String[] info = content.split(" ");
            if (info.length != 3) {
                Main.output("Invalid tft command with " + (info.length - 2) + " args instead of 1 by " + user);
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", 
                    "Please view the command usage below.\n\n`.tft start <instance>`" 
                    + "\n\nReplace <instance> with the lobby letter (A-Z)")).queue();
                return;
            }

            String instance = info[2].toUpperCase();

            lobbyPlayers = new ArrayList<User>();

            // TFT set variable
            String title = "TFTacticians - Lobby " + instance;
            String desc = "Be the last person standing in a round-based strategy game that pits you against"
            + " seven opponents in a free-for-all race to build a powerful team that fights on your behalf.";
            
            tftlobby = new Embed(title, desc);

            // Consumer used without lambda - Consumer required for lobbyPost = message;
            post.sendMessage(guild.getPublicRole().getAsMention()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    post.deleteMessageById(message.getId()).queue();
                }
            });
            post.sendMessage(tftlobby.getTFTLobbyEmbed(event.getGuild())).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    Main.output("TFT Lobby message ID (" + message.getId() + ")");
                    lobbyPost = message;
                    post.addReactionById(lobbyPost.getId(), "\uD83C\uDFC6").queue();
                }
            });
            channel.sendMessage(Embed.successEmbed("TFT Lobby Created", "Successfully created a lobby in the " + 
                post.getAsMention() + " channel")).queue();
            Main.output("New TFT Lobby " + instance + " created by " + user);
        }
        else if (content.startsWith(".tft")) {
            if (!member.getRoles().contains(Constants.getModRole(guild)) && !Constants.isWhitelist(user)) {
                Main.output("Invalid tft command by " + user + " - Missing Moderator role");
                return;
            }
            Main.output("Invalid .tft command without a clause by " + user);
                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below.\n\n`.tft start\n.tft end`")).queue();
                return;
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
        String discordID = user.getName() + "#" + user.getDiscriminator();
        if (!Database.checkDiscordExists(discordID)) {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Registration Failed", Constants.registrationFailDiscord(discordID))).queue();
            });
            return;
        }

        if (!lobbyPlayers.contains(event.getUser())) {
            lobbyPlayers.add(event.getUser());
            Main.output("New lobby registration by user " + event.getUser());

            lobbyPost.editMessage(tftlobby.getTFTLobbyEmbed(event.getGuild())).queue();
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

            lobbyPost.editMessage(tftlobby.getTFTLobbyEmbed(event.getGuild())).queue();
        }
    }

    /**
     * Closes the current lobby and shuffles the players
     * Can be called manually via '.lobby end' or when the lobby reaches the player limit
     */
    public void endLobby() {
        post.deleteMessageById(lobbyPost.getId()).queue(); // Delete lobby post
        lobbyPost = null; // Ends the lobby
    }

    public static ArrayList<User> getLobbyPlayers() {
        return lobbyPlayers;
    }
}

