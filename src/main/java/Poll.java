import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Handles custom polls
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.5
 */
public class Poll extends ListenerAdapter {

    private static ArrayList<User> pollUsers;
    private static int[] votes;
    private Message pollMessage;
    private Embed poll;
    private static String[] emoji = {"\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", 
    "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3"};

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message msg = event.getMessage();
        String content = msg.getContentRaw();

        if (content.startsWith(".poll")) {
            User user = event.getAuthor();
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();
            Guild guild = event.getGuild();
            
            if (!member.getRoles().contains(Constants.getCommitteeRole(guild))) {
                Main.output("Invalid poll command by " + user + " - Missing committee role");
                return;
            }
            
            if (content.equals(".poll end")) {
                channel.deleteMessageById(msg.getId()).queue();
                endPoll();
                return;
            }

            String[] info = content.split("\n");
            if (info.length < 5) {
                Main.output("Invalid poll command with " + (info.length - 1) + " args instead of a minimu of 5 by " + user);

                // Error Message
                channel.sendMessage(Embed.errorEmbed("Invalid Command", "Please view the command usage below" +
                        "\n\n`.poll\nTitle\nDescription\nOption 1\nOption 2`\n\nNote that you may add up to 9 options")).queue();
                return;
            }
            String title = info[1].trim();
            String description = info[2].trim();
            String[] options = Arrays.copyOfRange(info, 3, info.length);

            poll = new Embed(title, description, options);
            pollUsers = new ArrayList<>();
            votes = new int[info.length - 3];

            channel.deleteMessageById(msg.getId()).queue();

            // Sends an @everyone message and deletes it
            channel.sendMessage(guild.getPublicRole().getAsMention()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    channel.deleteMessageById(message.getId()).queue();
                }
            });

            channel.sendMessage(poll.getPollEmbed()).queue(new Consumer<Message>() {
                public void accept(Message message) {
                    Main.output("Poll ID (" + message.getId() + ")");
                    pollMessage = message;

                    for (int i = 0; i < options.length; i++) {
                        channel.addReactionById(pollMessage.getId(), emoji[i]).queue();
                    }
                }
            });
            Main.output("Successfully created a new poll (U:" + user + ")");
        }   
    }  

     /**
     * Handles poll voting through reaction
     * @param event Reaction add event
     */
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (pollMessage == null || event.getUser().isBot() || !event.getMessageId().equals(pollMessage.getId())) {
            return;
        }
        User user = event.getUser();
        Guild guild = event.getGuild();

        // Checking for Member role
        if (!event.getMember().getRoles().contains(Constants.getMemberRole(event.getGuild()))) {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Poll Vote Failed", Constants.registrationFailRole(guild))).queue();
            });
            return;
        }

        // Check if user has already voted
        if (!pollUsers.contains(event.getUser())) {
            pollUsers.add(event.getUser());

            String selection = event.getReactionEmote().getName();
            for (int i = 0; i < votes.length; i++) {
                if (selection.equals(emoji[i])) {
                    Main.output("New poll '" + (i+1) + "' vote by user " + event.getUser());
                    votes[i] += 1;
                    continue;
                }
            }

            pollMessage.editMessage(poll.getPollEmbed()).queue();
        } else {
            user.openPrivateChannel().queue((privateChannel) -> {
                privateChannel.sendMessage(Embed.errorEmbed("Poll Vote Failed", "You have already voted for another option and cannot change your vote.")).queue();
            });
        }
    }

    public void endPoll() {
        pollMessage.editMessage(poll.getPollEmbedEnd()).queue();
        pollMessage = null;
    }

    public static int[] getPollVotes() {
        return votes;
    }

    public static String[] getEmoji() {
        return emoji;
    }
}