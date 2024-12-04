# Terminal-Trader
<pre>
A text-based, stock trader within the terminal.

Current Version: v1.0.4
Game Name: Terminal Trader
Shorthand: tetrad
</pre>
Creating New Installer For Windows (Current Method):
1. When the code is ready run, generate a .jar file:
```powershell
   mvn clean package
```
2. Use JLink for creating minimal runtime environment
```powershell
jlink `
  --module-path "C:\Program Files\Java\jdk-22.0.2\jmods" <#example path#>`
  --add-modules java.base,java.desktop `
  --output runtime `
  --compress 2 `
  --strip-debug `
  --no-header-files `
  --no-man-pages
```
2. Then open Inno Setup, run the installer with the proper versions and jars
3. The finished installer will be named "Terminal-Trader-X.X.X-Installer.exe" on the left

Creating New Installer For Mac (Current Method)
1. When the code is ready, generate a .jar file
```powershell
   mvn clean package
```
3. Use JPackage to create 
