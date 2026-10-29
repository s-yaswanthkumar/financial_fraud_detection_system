@echo off
cd /d "%~dp0"
javac -sourcepath . -d target/classes VerificationRunner.java
java -cp target/classes VerificationRunner
pause
