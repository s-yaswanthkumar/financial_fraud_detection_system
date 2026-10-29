@echo off
cd /d "%~dp0"
javac -sourcepath . -d target/classes ui/ConsoleApp.java
java -cp target/classes ui.ConsoleApp
pause
