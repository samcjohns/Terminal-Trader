Current Version: v0.12
Game Name: Terminal Trader
Shorthand: tetrad

PLAN: 
To be ready for 1.0:

Update Ideas:
- More random events
- Settings for how the game behaves (affects constants)
- Print something after each advance, like how much money gained or lost
- File that holds names of all active users, can be used in the "Load Game" menu
  - this would also solve the overwriting issue
- History of stock purchases with time stamps
- ChatGPT written descriptions for stocks
- System.in.read() for instant commands rather than numbers and enter
- Add "Command Tray" at the bottom with all possible commands for the given menu
- Add 100 Day change to main stock menu
- "Challenge Saves" saves that lock down at 

FIXME:
- Portfolio net worth incorrect, graph prints wrong as well (perhaps)
- Add Average value to main stock menu
- Stop pushing references around, make them package private and access them from Game (game.mkt)

1. Move Advance to 1. position in menu
2. Have sell stock from portfolio
3. have buy stock from market (showing graph and performance)
4. Add "User Info" to menu
5. Add "News Feed" to menu

TO CREATE NEW INSTALL:
1. When the code is ready run this in the terminal:
    mvn clean package
2. Then open Inno Setup, run the installer with the proper versions and jars
3. The finished installer will be named "Terminal-Trader-X.X-Installer.exe" on the left