import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main class for Minion Discord bot.
 * Written for the UWA League Club Discord server.
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.3
 */
public class Main extends ListenerAdapter {
    public static Database db;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static JDA jda;

    public static void main(String[] args) {
        try {
            jda = new JDABuilder(Constants.getToken()).build();
            jda.addEventListener(new Register());
            jda.addEventListener(new AutoRole());
            jda.addEventListener(new Lobby());
            jda.addEventListener(new Points());
            jda.addEventListener(new Profile());
            jda.addEventListener(new Leaderboard());
            jda.addEventListener(new TFT());
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setGame(Game.playing(Constants.STATUS));
        } catch (Exception e) {
            e.printStackTrace();
        }

        db = new Database();
        db.establishConnection();

        // Scheduler
        final Runnable rds = new Runnable() {
            @Override
            public void run() {
                rdsReboot();
                Main.output("Re-established connection with MySQL");
            }
        };
        scheduler.scheduleAtFixedRate(rds, 0, 5, TimeUnit.HOURS);
    }

    /**
     * Displays consistent logging with date formats
     * @param string Log to output
     */
    public static void output(String string) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Perth"));
        Date date = new Date();
        System.out.println(dateFormat.format(date) + " " + string);
    }
    
    private static void rdsReboot() {
        Database.establishConnection();
    }
}
