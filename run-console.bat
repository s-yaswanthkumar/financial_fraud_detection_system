@echo off
cd /d "%~dp0"
javac -sourcepath src/main/java -d target/classes src/main/java/com/fraud/detection/ui/ConsoleApp.java
java -cp target/classes com.fraud.detection.ui.ConsoleApp
pause
