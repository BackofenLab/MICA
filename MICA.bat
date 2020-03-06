@echo off

REM # check if JAVA home path is set 

IF DEFINED JAVA_HOME (set JCALL="%JAVA_HOME%\bin\java") ELSE (set JCALL=java)

REM # run Online Role Play Board

%JCALL% -jar mica.jar
