import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* RiotAPI configurations and rank fetching
*
* @author Bruce How
* @github brucehow
* @version 1.0
*/

public class RiotAPI {

	private Map<String, Integer> tierMMR;
    private Map<String, Integer> divisionMMR;
    public static final String summoner = "https://oc1.api.riotgames.com/lol/summoner/v4/summoners/by-name/";
    public static final String encrypted = "https://oc1.api.riotgames.com/lol/summoner/v4/summoners/";
    public static final String league = "https://oc1.api.riotgames.com/lol/league/v4/entries/by-summoner/";

    

	public RiotAPI() {
		tierMMR = new HashMap<>();
		tierMMR.put("IRON", 0);
		tierMMR.put("BRONZE", 500);
		tierMMR.put("SILVER", 1000);
		tierMMR.put("GOLD", 1500);
		tierMMR.put("PLATINUM", 2000);
		tierMMR.put("DIAMOND", 2500);
		tierMMR.put("MASTER", 3000);
		tierMMR.put("GRANDMASTER", 3500);
		tierMMR.put("CHALLENGER", 4000);

		divisionMMR = new HashMap<>();
		divisionMMR.put("IV", 100);
		divisionMMR.put("III", 200);
		divisionMMR.put("II", 300);
        divisionMMR.put("I", 400);
    }
    
    private static String fetch(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        urlToRead += "?api_key=" + Constants.API_KEY;
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static String getSummonerEncryptedID(String ign) {
        try {
            String htmlign = ign.replaceAll(" ", "%20");
            return new JSONObject(fetch(summoner + htmlign)).getString("id");
        } catch (Exception e) {
            Main.output("Failed to get summoner encrypted ID for ign " + ign);
            return null;
        }
    }

    public static String getSummonerName(String encrypted_id) {
        try {
            return new JSONObject(fetch(encrypted + encrypted_id)).getString("name");
        } catch (Exception e) {
            Main.output("Failed to get summoner name from encrypted id " + encrypted_id);
            return null;
        }
    }

    private static ArrayList<Rank> getSummonerRank(String ign) {
        ArrayList<Rank> res = new ArrayList<>();
        try {
            String encryptedID = getSummonerEncryptedID(ign);
            if (encryptedID == null) {
                return null;
            }
            JSONArray ranks = new JSONArray(fetch(league + encryptedID));
            
            // Gets the highest rank
            for (int i = 0; i < ranks.length(); i++) {
                JSONObject obj = ranks.getJSONObject(i);
                res.add(new Rank(obj.getString("tier"), obj.getString("rank")));
            }
            return res;
        } catch (Exception e) {
            Main.output("Failed to get summoner rank");
            Main.output(e.toString());
            return null;
        }
    }

    private static ArrayList<Rank> getEncryptedRank(String encrypted) {
        ArrayList<Rank> res = new ArrayList<>();
        try {
            if (encrypted == null) {
                return null;
            }
            JSONArray ranks = new JSONArray(fetch(league + encrypted));
            
            // Gets the highest rank
            for (int i = 0; i < ranks.length(); i++) {
                JSONObject obj = ranks.getJSONObject(i);
                res.add(new Rank(obj.getString("tier"), obj.getString("rank")));
            }
            return res;
        } catch (Exception e) {
            Main.output("Failed to get summoner rank");
            Main.output(e.toString());
            return null;
        }
    }

    
	public int getSummonerMMR(String ign) {
        ArrayList<Rank> ranks = getSummonerRank(ign);

		if (ign == null || ranks.size() == 0) {
			return Constants.UNKNOWN_MMR;
        }
        int highestMMR = 0;
        for (Rank rank : ranks) {
            int MMR = tierMMR.get(rank.tier) + divisionMMR.get(rank.division);
            if (highestMMR <= MMR) {
                highestMMR = MMR;
            }
        }
        Main.output("Successfully fetched summoner " + ign + " MMR as " + highestMMR);
        return highestMMR == 0 ? Constants.DEFAULT_MMR : highestMMR;
    }

    public int getEncryptedMMR(String encrypted) {
        ArrayList<Rank> ranks = getEncryptedRank(encrypted);

		if (encrypted == null) {
			return Constants.UNKNOWN_MMR;
        }
        int highestMMR = 0;
        for (Rank rank : ranks) {
            int MMR = tierMMR.get(rank.tier) + divisionMMR.get(rank.division);
            if (highestMMR <= MMR) {
                highestMMR = MMR;
            }
        }
        Main.output("Successfully fetched summoner " + encrypted + " MMR as " + highestMMR);
        return highestMMR == 0 ? Constants.DEFAULT_MMR : highestMMR;
    }
     
	public static String getSummonerInfoString(String discord_id) {
        String ign = Database.getSummonerFromDiscordID(discord_id);
        String infoString = "Unknown (Unranked)";
		if (ign == null) {
			return infoString;
        }
        String encryptedID = Database.getEncryptedFromDiscordID(discord_id);
        ArrayList<Rank> ranks = getEncryptedRank(encryptedID);
        int highestMMR = 0;
        for (Rank rank : ranks) {
            RiotAPI api = new RiotAPI();
            int MMR = api.tierMMR.get(rank.tier) + api.divisionMMR.get(rank.division);
            if (highestMMR <= MMR) {
                highestMMR = MMR;
                infoString = ign + " (" + rank.tier.substring(0, 1) + rank.tier.substring(1).toLowerCase() + " " + rank.division + ")";
            }
        }
		return highestMMR == 0 ? ign + " (Unranked)" : infoString;
    }
}
