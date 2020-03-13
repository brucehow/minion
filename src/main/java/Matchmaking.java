import net.dv8tion.jda.core.entities.User;

import java.util.*;

/**
 * Match make players into fair teams
 *
 * @author Bruce How
 * @github brucehow
 * @version 1.0
 */
public class Matchmaking {

    private ArrayList<User> team1;
    private ArrayList<User> team2;
    private int team1MMR;
    private int team2MMR;
    private HashMap<User, Integer> summonerInfo;
    private RiotAPI api;

    public Matchmaking(ArrayList<User> players) {
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();
        team1MMR = 0;
        team2MMR = 0;
        api = new RiotAPI();
        summonerInfo = new HashMap<>();
        for (User user : players) {
            String encrypted = Database.getEncryptedFromDiscordID(user.getId());
            summonerInfo.put(user, api.getEncryptedMMR(encrypted));
        }
    }

    public ArrayList<User> getTeam1() {
        return team1;
    }

    public ArrayList<User> getTeam2() {
        return team2;
    }

    public int getTeam1MMR() {
        return team1MMR;
    }

    public int getTeam2MMR() {
        return team2MMR;
    }

    public void bruceMM() {
        int[] summonerMMR = summonerInfo.values().stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(summonerMMR);
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();
        team1MMR = 0;
        team2MMR = 0;

        int teamSize = (int) Math.ceil(summonerMMR.length/2);
        Main.output("Drafting teams of " + teamSize + " with Bruce's MM algorithm");

        ArrayList<User> allocated = new ArrayList<>();

        for (int i = summonerMMR.length-1; i >= 0; i--) {
            if (team1MMR <= team2MMR) {
                for (User player : summonerInfo.keySet()) {
                    if (summonerInfo.get(player) == summonerMMR[i] && !allocated.contains(player)) {
                        allocated.add(player);
                        if (team1.size() == teamSize) {
                            team2.add(player);
                            team2MMR += summonerMMR[i];
                            for (int j = i-1; j >= 0; j--) {
                                for (User d : summonerInfo.keySet()) {
                                    if (!allocated.contains(d)) {
                                        team2.add(d);
                                        team2MMR += summonerMMR[j];
                                        allocated.add(d);
                                    }
                                }
                            }
                        } else {
                            team1MMR += summonerMMR[i];
                            team1.add(player);
                            break;
                        }
                    }
                }
            } else {
                for (User player : summonerInfo.keySet()) {
                    if (summonerInfo.get(player) == summonerMMR[i] && !allocated.contains(player)) {
                        allocated.add(player);
                        if (team2.size() == teamSize) {
                            team1.add(player);
                            team1MMR += summonerMMR[i];
                            for (int j = i-1; j >= 0; j--) {
                                for (User d : summonerInfo.keySet()) {
                                    if (!allocated.contains(d)) {
                                        team1.add(d);
                                        team1MMR += summonerMMR[j];
                                        allocated.add(d);
                                    }
                                }
                            }
                        } else {
                            team2MMR += summonerMMR[i];
                            team2.add(player);
                            break;
                        }
                    }
                }
            }
        }
        Main.output("Team 1 " + team1.toString() + " MMR: " + team1MMR + " AVG: " + Math.round(team1MMR/5));
        Main.output("Team 2 " + team2.toString() + " MMR: " + team2MMR + " AVG: " + Math.round(team2MMR/5));
    }
}
