@echo off

SET /P AREYOUSURE=Are you sure (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

echo ... rest of file ...

:END

echo ... end of file ...