# Minion &middot; [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/brucehow/Minion/blob/master/LICENSE) [![Java](https://img.shields.io/badge/java-8.1-blue.svg)]()
<b>Author:</b> [Bruce How](https://github.com/brucehow/)

Minion is a Java based discord bot used for the University of Western Australia League Club (UWALC).

This bot requires a functional database (jdbc used here) and a class containing constant variables. These files have been omitted to the repo for obvious reasons.

## Events
### **Rift Champions**
Rift Champions is a weekly event run by UWALC. It involves several 10-man in-house games where club members can gather points for their participation and performance. This event was on every Fridays between 8PM to 11PM and occured over the course of Semester 1.

Users who join the Rift Champions lobby will be automatically drafted into even teams. To ensure that these games are fair and have a competitive feel, Minion uses the API method Summoner-V4 and League-V4 to obtain each summoner's rank, and a Matchmaking algorithm to draft the players.

More info on the League of Legends API reference can be found [here](https://developer.riotgames.com/api-methods/).

## [2.1.0.b] - 2020-03-14

### Added
- Wiki information for project handover

## [2.1.0.a] - 2020-03-14

### Added
- Rules embed for generating rules
  - Users must comply with the rules to access the server
  - Guest/Member roles automatically assigned based on database entries

## [2.1.0] - 2020-03-13

### Added
- User polls for voting
- Additional leaderboard page
- Ranking system now fetches highest MMR (incl Flex queue)
- New manual RiotAPI using HTTP requests

### Removed
- TFT functionality
- TFTactician commands

### Changed
- Fixed typos profiles
- Changed database requirements from summoner ID to encrypted ID
- Changed database requirements from discord name and discriminator to discord user ID
- Change matchmaking systems
- Altered default MMR by splitting it into unknown and unranked MMR

## License
[MIT License](https://github.com/brucehow/Minion/blob/master/LICENSE), Copyright © Bruce How 2019
