@echo off
cd /d "%~dp0"
echo Starting Financial Fraud Detection Web Dashboard...
javac -sourcepath . -d target/classes web/WebServer.java
start http://localhost:8080
java -cp target/classes web.WebServer
pause
