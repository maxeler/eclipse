@echo off

set usage_str="Usage: %~n0 <path-to-eclipse> [--revert]"
set error_msg="[Error] Too many arguments. Use command with -h or --help for help."
set error_str1="[Error] Incorrect arguments. Use command with -h or --help for help"

set argc=0
for %%x in (%*) do set /A argc+=1

if %argc% lss 1 (
  java -jar Install.jar
  exit /b 0
)

if %argc% gtr 2 (
  echo %error_msg%
  exit /b 1
)

if %argc% equ 1 (
  if "%1" == "-h" (
    echo %usage_str%
    exit /b 1
  )
  if "%1" == "--help" (
    echo %usage_str%
    exit /b 1
  )
  java -jar Install.jar %1
  exit /b 1
)

if %argc% equ 2 (
  if "%2" == "--revert" (
    java -jar Install.jar %1 %2
  ) else (
      echo %error_str1%
      exit /b 1
    )
)

exit /b 0
