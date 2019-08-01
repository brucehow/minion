# Minion &middot; [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/brucehow/Minion/blob/master/LICENSE) [![Java](https://img.shields.io/badge/java-8.1-blue.svg)]()
<b>Author:</b> [Bruce How](https://github.com/brucehow/)

Minion is a Java based discord bot used for the University of Western Australia League Club (UWALC).


## Functionalities
### **Role Assignments**

Positional Role – Discord users are automatically assigned to their appropriate positional roles upon reaction.

Member Roles – Users who have signed up (member of UWALC) are automatically assigned the Member role. Users without this role have limited server accessibility.

### **Member Registrations**
Member registration can be completed with a few lines discord commands allowing for efficientcy.

### **Rift Champions**
Rift Champions is a weekly event run by UWALC. It involves several 10-man in-house games where club members can gather points for their participation and performance. This event was on every Fridays between 7PM to 11PM and occured over the course of Semester 1.

Users who join the Rift Champions lobby will be automatically drafted into even teams. To ensure that these games are fair and have a competitive feel, Minion uses the API method Summoner-V4 and League-V4 to obtain each summoner's rank, and a Matchmaking algorithm to draft the players.

More info on the League of Legends API reference can be found [here](https://developer.riotgames.com/api-methods/).

### **Teamfight Tacticians**
Teamfight Tacticians is another weekly event run by UWALC. It involves several 8-man in-house games featuring Riot's new game Teamfight Tactics. Minion is responsible for drafting players into teams of 8.

Due to the nature of the game (requires 8 players), it is not always possible for all players to be drafted into a team. The drafting algorithm takes into account players who miss out on games and will prioritise them for the next lobby. 

## License

[MIT License](https://github.com/brucehow/Minion/blob/master/LICENSE), Copyright © Bruce How 2019
