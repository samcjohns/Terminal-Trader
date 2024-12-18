[Setup]
AppName=Terminal Trader
AppVerName=Terminal Trader 1.1.1
DefaultDirName=C:\Program Files\Terminal Trader
DefaultGroupName=Terminal Trader
OutputDir=.
OutputBaseFilename=Terminal-Trader-1.1.1-Installer
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
MinVersion=6.1.7601
UsePreviousAppDir=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
Name: "{userappdata}\Terminal Trader\saves"
Name: "{userappdata}\Terminal Trader\gen"
Name: "{app}\wav"

[Files]
Source: ".\cmd\start-tetrad.cmd"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\cmd\get-sounds.cmd"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\target\tetrad-1.1.1.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\icons\tetradicon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{commondesktop}\Terminal Trader"; Filename: "{app}\start-tetrad.cmd"; IconFilename: "{app}\tetradicon.ico"; Tasks: desktopicon
Name: "{group}\Terminal Trader"; Filename: "{app}\start-tetrad.cmd"; WorkingDir: "{app}"; IconFilename: "{app}\tetradicon.ico"

[Run]
Filename: "{app}\get-sounds.cmd"; Description: "Download Sound Pack (Recommended)"; Flags: runhidden runascurrentuser postinstall skipifsilent
Filename: "{app}\start-tetrad.cmd"; Description: "Launch Terminal Trader"; Flags: nowait postinstall skipifsilent
