@echo off

rem a
set b=c

rem b
set c=d

rem d
set e=f

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

rem goto commandModule_3
rem commandModule_3
echo fff
ddd
:commandModule_3

