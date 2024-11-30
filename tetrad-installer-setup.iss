[Setup]
AppName=Terminal Trader
AppVerName=Terminal Trader 1.0.1
DefaultDirName={pf}\TerminalTrader
DefaultGroupName=Terminal Trader
OutputDir=.
OutputBaseFilename=Terminal-Trader-1.0.1-Installer
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
MinVersion=6.1.7601

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
Name: "{userappdata}\TerminalTrader\saves"
Name: "{userappdata}\TerminalTrader\gen"

[Files]
Source: ".\cmd\start-1.0.1.cmd"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\target\tetrad-1.0.1.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\icons\tetradicon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{commondesktop}\Terminal Trader"; Filename: "{app}\start-1.0.1.cmd"; IconFilename: "{app}\tetradicon.ico"; Tasks: desktopicon
Name: "{group}\Terminal Trader"; Filename: "{app}\start-1.0.1.cmd"; WorkingDir: "{app}"; IconFilename: "{app}\tetradicon.ico"

[Run]
Filename: "{app}\start-1.0.1.cmd"; Description: "Launch Terminal Trader"; Flags: nowait postinstall skipifsilent
