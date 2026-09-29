# Minesweeper for MCphone
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
> A complete port of Windows XP Minesweeper into the MCphone phone screen, faithfully recreating the original pixel-art look, sound effects and gameplay.
![logo](logo.png)
A NeoForge client-side addon for [mcphone](https://github.com/november521/mcphone): a fully playable WinXP Minesweeper inside your MCphone phone screen.

Read this in: [English](README.md) | [Chinese](README_zh.md)

## Download
- **CurseForge**: https://www.curseforge.com/minecraft/mc-mods/minesweeper-for-mcphone
- **Nexus Mods**: https://www.nexusmods.com/minecraft/mods/1347
- **GitHub Releases**: https://github.com/ytbetafish/mcphone-minesweeper/releases

## Features
- Full Minesweeper gameplay: Beginner 9x9 / Intermediate 16x16 / Expert 30x16, plus fully custom boards (width / height / mine count)
- Animated smiley face (normal / pressed / scared / dead / winner) - click anytime to restart
- LED-style counters: remaining mines and elapsed time, with correct negative-number rendering
- Complete in-game menu: new game, ?-mark toggle, color toggle, sound toggle, best times, and Beginner / Intermediate / Expert / Custom difficulties
- Custom game dialog: numeric inputs with +/- stepper buttons, with automatic validation against legal limits (e.g. a 2x2 board allows at most 3 mines)
- Flag / Question tool buttons: MCphone cannot capture right-click input, so flagging and question-marking are two mutually exclusive buttons with proper pressed states
- Best Times leaderboard for all three difficulties
- Original sound effects (click / lose / win)
- TNT unlock purchase (MCphone app store, same as stock MCphone apps)

## Requirements
| Item | Version |
|---|---|
| Minecraft | 1.21.1 (Java 21) |
| NeoForge | 21.1.200+ |
| mcphone | 1.10.2+ (required) |

## Installation
1. Install the required **mcphone** mod (v1.10.2+): <https://github.com/november521/mcphone/releases>
2. Put `mcphone_minesweeper-*.jar` into the `.minecraft/mods` folder
3. Launch the game, open Minesweeper on the MCphone home screen, and unlock it with TNT in the app store

## Building from source
```bash
# 1. Put mcphone-1.10.2-1.21.1-neoforge.jar into libs/ (see libs/README.md)
# 2. Build
./gradlew jar
# Output: build/libs/mcphone_minesweeper-<version>.jar
```

## Structure
```
src/main/java/com/mcphoneminesweeper/   mod code (page / sprites / pricing)
src/main/resources/                    assets (textures / sounds / lang / metadata)
```

## License
Code is open source under the [MIT](LICENSE) license.
- Graphics and layout reference the original Windows XP Minesweeper
- Sounds extracted from the original winmine.exe, copyright Microsoft Corporation

## Credits
- [mcphone](https://github.com/november521/mcphone) - the phone mod framework
- Windows XP Minesweeper - gameplay and art inspiration
