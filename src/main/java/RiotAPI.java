import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import org.apache.commons.text.WordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* RiotAPI configurations and rank fetching
*
* @author Bruce How
* @github brucehow
* @version 1.0
*/
public class RiotAPI {

	private RiotApi api;
	private Map<String, Integer> tierMMR;
    private Map<String, Integer> divisionMMR;

	public RiotAPI() {
		ApiConfig config = new ApiConfig().setKey(Constants.API_KEY);

		api = new RiotApi(config);

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

	public int fetchMMR(String player) {
		if (player == null) {
			return Constants.DEFAULT_MMR;
        }
		try {
			Summoner summoner = api.getSummonerByName(Platform.OCE, player);
			Set<LeaguePosition> summonerRanks = api.getLeaguePositionsBySummonerId(Platform.OCE, summoner.getId());
			for (LeaguePosition type : summonerRanks) {
				if (type.getQueueType().equals("RANKED_SOLO_5x5")) {
					int MMR = tierMMR.get(type.getTier()) + divisionMMR.get(type.getRank());
					Main.output("Successfully fetched summoner " + player + " " + type.getTier() + " " + type.getRank()
							+ " - " + MMR + " MMR");
					return MMR;
				}
			}
		} catch (RiotApiException e) {
			Main.output("Fetched summoner " + player + " NONEXISTENT - 1200 MMR");
			return Constants.DEFAULT_MMR;
		}
		Main.output("Fetched summoner " + player + " UNRANKED - 1200 MMR");
		return Constants.DEFAULT_MMR;
	}


	public static String fetchSummonerInfoString(String discord) {
		String summonerName = Database.getSummonerFromDiscord(discord);
		if (summonerName == null) {
			return "Unknown (Unranked)";
        }
		try {
			ApiConfig config = new ApiConfig().setKey(Constants.API_KEY);
			RiotApi api = new RiotApi(config);
			Summoner summoner = api.getSummonerByName(Platform.OCE, summonerName);
			Set<LeaguePosition> summonerRanks = api.getLeaguePositionsBySummonerId(Platform.OCE, summoner.getId());
			for (LeaguePosition type : summonerRanks) {
				if (type.getQueueType().equals("RANKED_SOLO_5x5")) {
					String rank = "";
					rank += WordUtils.capitalize(type.getTier().toLowerCase()) + " " + type.getRank();
					return summonerName + " (" + rank + ")";
				}
			}
		} catch (RiotApiException e) {
			Main.output(e.toString());
			return summonerName + " (Unranked)";
		}
		return summonerName + " (Unranked)";
	}
}


