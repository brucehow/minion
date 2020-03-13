import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
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
    public static Guild ULC;

    public static void main(String[] args) {       
        try {
            jda = new JDABuilder(Constants.getToken()).build();
            jda.addEventListener(new Register());
            jda.addEventListener(new AutoRole());
            jda.addEventListener(new Lobby());
            jda.addEventListener(new Points());
            jda.addEventListener(new Profile());
            jda.addEventListener(new Leaderboard());
            jda.addEventListener(new Dev());
            jda.addEventListener(new Poll());
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setGame(Game.playing(Constants.STATUS));
        } catch (Exception e) {
            e.printStackTrace();
        } 

        Main.output("Attempting to connect to MySQL");
        Database.establishConnection(true);

        try {
            jda.awaitReady();
        } catch (Exception e) {
            Main.output(e.toString());
        }
        Main.output("JDA fully loaded");
        ULC = jda.getGuildById(Constants.GUILD_ID);

        // Scheduler
        final Runnable rds = new Runnable() {
            @Override
            public void run() {
                rdsReboot();
            }
        };
        final Runnable summonerUpdate = new Runnable() {
            @Override
            public void run() {
                Main.output("Running summoner name auto update");
                Dev.autoUpdateSummoner(ULC);
            }
        };
        scheduler.scheduleAtFixedRate(summonerUpdate, 0, 24, TimeUnit.HOURS);
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
    
    public static void rdsReboot() {
        Database.establishConnection(false);
        Main.output("Attempting to re-established connection with MySQL");
    }
}
