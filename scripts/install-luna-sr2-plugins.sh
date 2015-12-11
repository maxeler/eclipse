#!/bin/sh
usage_str="Usage: $0 <path-to-eclipse-luna-SR2> [-revert]"
error_msg="Make sure the correct Eclipse Luna SR2 path has been passed"

checkConfigFile () {
  local config_file
  config_file=$1
  if [ ! -r $config_file ]; then
    echo "Cannot read configuration file: $config_file"
    echo $error_msg
    exit 1
  fi
  if [ ! -w $config_file ]; then
    echo "Cannot write configuration file: $config_file"
    echo $error_msg
    exit 1
  fi
}

checkPluginsDir () {
  local plugins_dir=$1
  if [ ! -d $plugins_dir -o ! -w $plugins_dir ]; then
    echo "Cannot write to plugins directory: $plugins_dir"
    echo $error_msg
    exit 1
  fi
}

findPluginEntry () {
  local plugin_name
  local entry_str
  local config_file
  local ec
  plugin_name=$1
  entry_str=$2
  config_file=$3
  grep -e $entry_str $config_file > /dev/null
  ec=$?
  if [ $ec -eq 1 ]; then
    echo "Could not find $plugin_name entry in the configuration file: $config_file"
    echo $error_msg
    exit 1
  fi
  if [ $ec -ge 2 ]; then
    echo "An error has occurred while reading the configuration file: $config_file"
    exit $ec
  fi
}

installPlugin () {
  local plugin_file
  local plugin_str
  local plugin_replacement_str
  local plugins_dir
  local config_file
  local ec
  plugin_file=$1
  plugin_str=$2
  plugin_replacement_str=$3
  plugins_dir=$4
  config_file=$5
  cp $plugin_file $plugins_dir
  ec=$?
  if [ $ec -ne 0 ]; then
    echo "An error has occurred while copying $plugin_file to plugins directory: $plugins_dir"
    exit $ec
  fi
	editConfigurationFile $plugin_str $plugin_replacement_str $config_file
}

editConfigurationFile () {
	local plugin_str
	local plugin_replacement_str
	local config_file
	local ec
	plugin_str=$1
	plugin_replacement_str=$2
	config_file=$3
	local os=`uname`
	if [ $os = "Darwin" ]; then
		sed -i '' "s/$plugin_str/$plugin_replacement_str/g" $config_file
	else
		sed -i "s/$plugin_str/$plugin_replacement_str/g" $config_file
	fi
  ec=$?
  if [ $ec -ne 0 ]; then
    echo "An error has occurred while editing the configuration file: $config_file"
    exit $ec
  fi
}

revertPluginInstallation () {
	local plugin_name
	local plugins_dir
	local config_file
	local plugin_str
	local plugin_replacement_str
	local plugin_file
	local ec
	plugin_name=$1
	plugins_dir=$2
	config_file=$3
	plugin_str=$4
	plugin_replacement_str=$5
	plugin_file=$6
	editConfigurationFile $plugin_replacement_str $plugin_str $config_file
	find $plugins_dir -name $plugin_file > /dev/null 2>&1
	ec=$?
	if [ $ec -ne 0 ]; then
		echo "$plugin_file not found in plugins directory: $plugins_dir"
	fi
	rm -f $plugins_dir/$plugin_file
	ec=$?
	if [ $ec -ne 0 ]; then
		echo "An error has occurred while removing $plugins_dir/$plugin_file"
	fi
}

if [ $# -lt 1 ]; then
  echo $usage_str
  exit 1
fi
if [ "$1" = "-h" -o "$1" = "--help" ]; then
  echo $usage_str
  exit 0
fi

plugins_dir="$1/plugins"
config_file="$1/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"
jdtcore_str="org.eclipse.jdt.core,3.10.2.v20150120-1634,plugins\/org.eclipse.jdt.core_3.10.2.v20150120-1634.jar,4,false"
jdtui_str="org.eclipse.jdt.ui,3.10.2.v20141014-1419,plugins\/org.eclipse.jdt.ui_3.10.2.v20141014-1419.jar,4,false"
jdtcore_file="org.eclipse.jdt.core-3.10.2-MAXELER-v0.1-alpha.jar"
jdtui_file="org.eclipse.jdt.ui-3.10.2-MAXELER-v0.1-alpha.jar"
jdtcore_replacement_str="org.eclipse.jdt.core,3.10.2.v20151204-1038,plugins\/$jdtcore_file,4,false"
jdtui_replacement_str="org.eclipse.jdt.ui,3.10.2.v20151204-1038,plugins\/$jdtui_file,4,false"
checkPluginsDir $plugins_dir
checkConfigFile $config_file
if [ "$2" = "-revert" ]; then
	findPluginEntry "org.eclipse.jdt.core" $jdtcore_replacement_str $config_file
	findPluginEntry "org.eclipse.jdt.ui" $jdtui_replacement_str $config_file
	revertPluginInstallation "org.eclipse.jdt.core" $plugins_dir $config_file $jdtcore_str $jdtcore_replacement_str $jdtcore_file
	revertPluginInstallation "org.eclipse.jdt.ui" $plugins_dir $config_file $jdtui_str $jdtui_replacement_str $jdtui_file
	echo "Revert finished"
else
	findPluginEntry "org.eclipse.jdt.core" $jdtcore_str $config_file
	findPluginEntry "org.eclipse.jdt.ui" $jdtui_str $config_file
	installPlugin $jdtcore_file $jdtcore_str $jdtcore_replacement_str $plugins_dir $config_file
	installPlugin $jdtui_file $jdtui_str $jdtui_replacement_str $plugins_dir $config_file
	echo "Installation successful"
fi

exit 0
