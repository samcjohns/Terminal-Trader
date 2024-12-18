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
Source: ".\cmd\start-1.1.1.cmd"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\cmd\get-sounds-1.1.1.cmd"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\target\tetrad-1.1.1.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\icons\tetradicon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: ".\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{commondesktop}\Terminal Trader"; Filename: "{app}\start-1.1.1.cmd"; IconFilename: "{app}\tetradicon.ico"; Tasks: desktopicon
Name: "{group}\Terminal Trader"; Filename: "{app}\start-1.1.1.cmd"; WorkingDir: "{app}"; IconFilename: "{app}\tetradicon.ico"

[Run]
Filename: "{app}\get-sounds-1.1.1.cmd"; Description: "Download Sound Pack (Recommended)"; Flags: runhidden runascurrentuser postinstall skipifsilent
Filename: "{app}\start-1.1.1.cmd"; Description: "Launch Terminal Trader"; Flags: nowait postinstall skipifsilent
