@echo off
cd /d "%~dp0"
javac -sourcepath src/main/java -d target/classes src/main/java/com/fraud/detection/VerificationRunner.java
java -cp target/classes com.fraud.detection.VerificationRunner
pause
