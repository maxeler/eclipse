@echo off
setlocal EnableDelayedExpansion
set eclipse_name=Eclipse Luna SR2
set release_version=v0.1-alpha
set error_msg=Make sure the correct %eclipse_name% path has been passed

set argc=0
for %%x in (%*) do set /A argc+=1
if %argc% lss 1 (
	set chosen_option=0
	set /p opt=Please choose [i]nstall or [r]evert: 
	if "!opt!"=="i" ( set chosen_option=1
	) else ( if "!opt!"=="install" ( set chosen_option=1
	) else ( if "!opt!"=="I" ( set chosen_option=1
	) else ( if "!opt!"=="Install" ( set chosen_option=1
	) else ( if "!opt!"=="INSTALL" ( set chosen_option=1
	) else ( if "!opt!"=="r" ( set chosen_option=2
	) else ( if "!opt!"=="revert" ( set chosen_option=2
	) else ( if "!opt!"=="R" ( set chosen_option=2
	) else ( if "!opt!"=="Revert" ( set chosen_option=2
	) else ( if "!opt!"=="REVERT" ( set chosen_option=2
	))))))))))
	if !chosen_option! equ 0 (
		echo Invalid option
		exit /b 1
	)
	set /p eclipse_path=Please provide the path for %eclipse_name%: 
) else (
	set eclipse_path=%1
	if "%2"=="/revert" ( set chosen_option=2
	)
)

set plugins_dir=%eclipse_path%\plugins
set config_file=%eclipse_path%\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
set jdtcore_str="org.eclipse.jdt.core,3.10.2.v20150120-1634,plugins/org.eclipse.jdt.core_3.10.2.v20150120-1634.jar,4,false"
set jdtui_str="org.eclipse.jdt.ui,3.10.2.v20141014-1419,plugins/org.eclipse.jdt.ui_3.10.2.v20141014-1419.jar,4,false"
set jdtcore_file=org.eclipse.jdt.core-3.10.2-MAXELER-%release_version%.jar
set jdtui_file=org.eclipse.jdt.ui-3.10.2-MAXELER-%release_version%.jar
set jdtcore_replacement_str="org.eclipse.jdt.core,3.10.2.v20151204-1038,plugins/%jdtcore_file%,4,false"
set jdtui_replacement_str="org.eclipse.jdt.ui,3.10.2.v20151204-1038,plugins/%jdtui_file%,4,false"
call :checkPluginsDir %plugins_dir%
if !errorlevel! geq 1 (
	exit /b !errorlevel!
)
call :checkConfigFile %config_file%
if !errorlevel! geq 1 (
	exit /b !errorlevel!
)

if !chosen_option! equ 2 (
	echo Reverting...
	call :findPluginEntry org.eclipse.jdt.core %jdtcore_replacement_str% %config_file%
	if !errorlevel! geq 1 (
		exit /b !errorlevel!
	)
	call :findPluginEntry org.eclipse.jdt.ui %jdtui_replacement_str% %config_file%
	if !errorlevel! geq 1 (
		exit /b !errorlevel!
	)
	call :revertPluginInstallation org.eclipse.jdt.core %plugins_dir% %config_file% %jdtcore_str% %jdtcore_replacement_str% %jdtcore_file%
	call :revertPluginInstallation org.eclipse.jdt.ui %plugins_dir% %config_file% %jdtui_str% %jdtui_replacement_str% %jdtui_file%
	echo Revert finished
) else (
	echo Installing...
	call :findPluginEntry org.eclipse.jdt.core %jdtcore_str% %config_file%
	if !errorlevel! geq 1 ( 
		exit /b !errorlevel!
	)
	call :findPluginEntry org.eclipse.jdt.ui %jdtui_str% %config_file%
	if !errorlevel! geq 1 ( 
		exit /b !errorlevel!
	)
	call :installPlugin %jdtcore_file% %jdtcore_str% %jdtcore_replacement_str% %plugins_dir% %config_file%
	if !errorlevel! geq 1 ( 
		exit /b !errorlevel!
		)
	call :installPlugin %jdtui_file% %jdtui_str% %jdtui_replacement_str% %plugins_dir% %config_file%
	if !errorlevel! geq 1 ( 
		exit /b !errorlevel!
	)
	echo Installation successful
)

if %argc% lss 1 ( pause )
endlocal
exit /b

:checkConfigFile
set config_file=%1
if not exist %config_file% (
	echo Cannot find configuration file: %config_file%
	echo %error_msg%
	exit /b 1
)
exit /b

:checkPluginsDir
set plugins_dir=%1
if not exist %plugins_dir% (
	echo Cannot find plugins directory: %plugins_dir%
	echo %error_msg%
	exit /b 1
)
exit /b

:findPluginEntry
set plugin_name=%1
set entry_str=%2
set config_file=%3
findstr %entry_str% %config_file% > nul
if !errorlevel! neq 0 (
	echo Could not find %plugin_name% entry in the configuration file: %config_file%
	exit /b 1
)
exit /b

:installPlugin
set plugin_file=%1
set plugin_str=%2
set plugin_replacement_str=%3
set plugins_dir=%4
set config_file=%5
set tmp_config_file=%config_file%.tmp
xcopy /Q /Y %plugin_file% %plugins_dir% > nul
if !errorlevel! neq 0 (
	echo An error has occurred while copying %plugin_file% to plugins directory: %plugins_dir%
	exit /b 1
)
call :editConfigurationFile %plugin_str% %plugin_replacement_str% %config_file%
exit /b

:editConfigurationFile
set plugin_str=%~1
set plugin_replacement_str=%~2
set config_file=%3
set tmp_config_file=%config_file%.tmp
set ln=^


for /F "tokens=*" %%A in (%config_file%) do (
	if %%A==!plugin_str! (
		set /p foo="!plugin_replacement_str!!ln!"<nul >> !tmp_config_file!
	) else (
		set /p foo="%%A!ln!"<nul >> !tmp_config_file!
	)
)
move /Y %tmp_config_file% %config_file% > nul
exit /b

:revertPluginInstallation
set plugin_name=%1%
set plugins_dir=%2%
set config_file=%3%
set plugin_str=%4%
set plugin_replacement_str=%5%
set plugin_file=%6%
call :editConfigurationFile %plugin_replacement_str% %plugin_str% %config_file%
dir -r %plugins_dir%\%plugin_file% > nul 2>&1
if !errorlevel! neq 0 (
	echo %plugin_file% not found in plugins directory: %plugins_dir%
	exit /b !errorlevel!
)
del /Q /F %plugins_dir%\%plugin_file%
if !errorlevel! neq 0 (
	echo An error has occurred while removing %plugins_dir%\%plugin_file%
)
exit /b
