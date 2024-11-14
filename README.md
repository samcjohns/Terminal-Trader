# Terminal-Trader
<pre>
A text-based, stock trader within the terminal.

Current Version: v0.12
Game Name: Terminal Trader
Shorthand: tetrad
</pre>
Creating New Installer (Current Method):
1. When the code is ready run this in the terminal:
```powershell
   mvn clean package
```
2. Use JLink for creating minimal runtime environment
```powershell
jlink `
  --module-path "C:\Program Files\Java\jdk-22.0.2\jmods" <#example path#>`
  --add-modules java.base `
  --output runtime `
  --compress 2 `
  --strip-debug `
  --no-header-files `
  --no-man-pages
```
2. Then open Inno Setup, run the installer with the proper versions and jars
3. The finished installer will be named "Terminal-Trader-X.X-Installer.exe" on the left
