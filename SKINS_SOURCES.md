# Medieval player-skin sources

The GitHub Actions workflow downloads real 64x64 **Classic / Steve-arm** PNG skins before Gradle compiles the mod. Each source is assigned to one NPC role. If one URL is temporarily unavailable, its bundled fallback PNG remains.

PixelPelt states that all original skins on its catalog are free to use, edit, share and sell without attribution. Reference: https://freeskineditor.com/minecraft-skins/ . The site's format guide confirms that Classic skins use 4-pixel arms and are 64x64 PNGs.

| NPC role | Skin design | Page |
|---|---|---|
| Traveler | Viking raider | https://freeskineditor.com/minecraft-skins/viking-raider/ |
| Caravan merchant | Pirate captain | https://freeskineditor.com/minecraft-skins/pirate-captain/ |
| Village merchant | Farmer boy | https://freeskineditor.com/minecraft-skins/farmer-boy/ |
| Worker | Lumberjack | https://freeskineditor.com/minecraft-skins/lumberjack/ |
| Guard | Young knight | https://freeskineditor.com/minecraft-skins/young-knight/ |
| Warrior | Flame knight | https://freeskineditor.com/minecraft-skins/flame-knight/ |
| Lord | Royal gown | https://freeskineditor.com/minecraft-skins/royal-gown/ |
| King | Shadow king | https://freeskineditor.com/minecraft-skins/shadow-king/ |
| Bandit | Masked rogue | https://freeskineditor.com/minecraft-skins/masked-rogue/ |

Direct Classic PNG links are defined in `scripts/download-medieval-skins.sh`. The role texture names match the renderer (`traveler.png`, `king.png`, and so on).
