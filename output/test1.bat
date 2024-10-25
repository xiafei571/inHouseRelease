@echo off

rem a
set b=c

rem c
set d=e

rem goto commandModule_1
rem commandModule_1
echo aaa
bbb
:commandModule_1

goto commandModule_2
rem commandModule_2
echo ccc
ddd
:commandModule_2

