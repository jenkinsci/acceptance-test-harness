@REM script for setting all the variables in order to run the ATH locally on windows
@REM use config.groovy in the same folder if desired uncomment below
@REM set CONFIG=%~dp0%config.groovy
set DISPLAY=:0
set INTERACTIVE=false
set BROWSER=remote-webdriver-firefox
set REMOTE_WEBDRIVER_URL=http://127.0.0.1:4444/wd/hub
set JENKINS_JAVA_OPTS=-Xmx1280m

@REM Jenkins binds to 0.0.0.0 (OMG) so we can use any network but the docker network.
@REM but we may as well use the default network
@echo off
FOR /f "tokens=3" %%F in ('netsh interface ipv4 show addresses "vEthernet (WSL)" ^| findstr /c:"IP Address:"') DO (
SET IP=%%F
)
FOR /f "tokens=3" %%F in ('netsh interface ipv4 show addresses "vEthernet (WSL (Hyper-V firewall))" ^| findstr /c:"IP Address:"') DO (
SET IP=%%F
)
IF NOT DEFINED IP (
  echo "*** ERROR could not find the docker interface - is docker started?"
  exit /b 1
)

@echo on
set SELENIUM_PROXY_HOSTNAME=%IP%
set TESTCONTAINERS_HOST_OVERRIDE=%IP%
set JENKINS_LOCAL_HOSTNAME=%IP%
@echo.
@echo To start the remote firefox container run the following command:
@echo docker run --shm-size=256m -d -p 127.0.0.1:4444:4444 -p 127.0.0.1:5900:5900 -e no_proxy=localhost -e SCREEN_WIDTH=1680 -e SCREEN_HEIGHT=1090 selenium/standalone-firefox:4.18.1

