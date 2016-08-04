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

findPlugin () {
  local plugin_name
  local destination
  plugin_name=$1
  destination=$2

  local plugin_file=$(find $destination -name "$plugin_name")

  if [[ -z "${plugin_file// }" ]]; then
    echo "$plugin_name plugin could not be found in $destination folder!"
    exit 1
  fi

  echo ${plugin_file##*/}
  exit 0
}

getBundleVersion () {
  local plugin_filename
  plugin_filename=$1

  local version=$(basename $plugin_filename .jar | awk -F'_' '{print $2}'i)
  if [[ -z "${version// }" ]]; then
    echo "Could not extract plugin bundle version from filename: $plugin_filename"
    exit 1
  fi

  echo ${version// }
  exit 0
}

findPluginEntry () {
  local plugin_name
  local plugin_bundle_version
  local config_file
  local plugin_filename
  plugin_name=$1
  plugin_bundle_version=$2
  config_file=$3
  plugin_filename=$4

  local ec

  local version_major=$(echo $plugin_bundle_version | awk -F'.' '{print $1}'i)
  local version_minor=$(echo $plugin_bundle_version | awk -F'.' '{print $2}'i)
  grep -e "^$plugin_name,$version_major.$version_minor.*,plugins/$plugin_filename" $config_file > /dev/null

  ec=$?
  if [ $ec -eq 1 ]; then
    echo "Could not find $plugin_name (version $version_major.$version_minor+) entry in the configuration file: $config_file"
    echo $error_msg
    exit 1
  fi

  if [ $ec -ge 2 ]; then
    echo "An error has occurred while reading the configuration file: $config_file"
    exit $ec
  fi
}

installPlugin () {
  local plugin_name
  local plugins_dir
  local config_file
  local bundle_version
  local plugin_file
  plugin_name=$1
  plugins_dir=$2
  config_file=$3
  bundle_version=$4
  plugin_file=$5

  cp $plugin_file $plugins_dir
  ec=$?
  if [ $ec -ne 0 ]; then
    echo "An error has occurred while copying $plugin_file to plugins directory: $plugins_dir"
    exit $ec
  fi
  editConfigurationFile $config_file "s|$plugin_name,.*,plugins/.*\.jar,\(.*\)|$plugin_name,$bundle_version,$plugin_file,\1|g"
}

editConfigurationFile () {
  local config_file
  local sed_expr
  config_file=$1
  sed_expr=$2
  local os=`uname`

  if [ $os = "Darwin" ]; then
    sed -i '' "$sed_expr" $config_file
  else
    sed -i "$sed_expr" $config_file
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
  local bundle_version
  local plugin_file
  plugin_name=$1
  plugins_dir=$2
  config_file=$3
  bundle_version=$4
  plugin_file=$5

  local old_filename=$(findPlugin "$plugin_name"$"_*.jar" $plugins_dir)
  if [ $? -ne 0 ]; then
    echo $old_filename
    exit 1
  fi
  echo $old_filename
  local old_version=$(getBundleVersion $old_filename)
  if [ $? -ne 0 ]; then
    echo $old_version
    exit 1
  fi

  editConfigurationFile $config_file "s|^$plugin_name,.*,plugins/.*MAXELER.*\.jar,\(.*\)|$plugin_name,$old_version,plugins/$old_filename,\1|g"

  find $plugins_dir -name $plugin_file > /dev/null 2>&1
  local ec=$?
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

checkPluginsDir $plugins_dir
checkConfigFile $config_file

if [ "$2" = "-revert" ]; then
  jdtcore_file=$(findPlugin 'org.eclipse.jdt.core*MAXELER*.jar' $plugins_dir)
  if [ $? -ne 0 ]; then
    echo $jdtcore_file
    exit 1
  fi
  jdtcore_bundle_version=$(getBundleVersion $jdtcore_file)
  if [ $? -ne 0 ]; then
    echo $jdtcore_bundle_version
    exit 1
  fi
  jdtui_file=$(findPlugin 'org.eclipse.jdt.ui*MAXELER*.jar' $plugins_dir)
  if [ $? -ne 0 ]; then
    echo $jdtui_file
    exit 1
  fi
  jdtui_bundle_version=$(getBundleVersion $jdtui_file)
  if [ $? -ne 0 ]; then
    echo $jdtui_bundle_version
    exit 1
  fi

  findPluginEntry "org.eclipse.jdt.core" $jdtcore_bundle_version $config_file "org\.eclipse\.jdt\.core-MAXELER.*\.jar"
  findPluginEntry "org.eclipse.jdt.ui" $jdtui_bundle_version $config_file "org\.eclipse\.jdt\.ui-MAXELER.*\.jar"
  revertPluginInstallation "org.eclipse.jdt.core" $plugins_dir $config_file $jdtcore_bundle_version $jdtcore_file
  revertPluginInstallation "org.eclipse.jdt.ui" $plugins_dir $config_file $jdtui_bundle_version $jdtui_file
  echo "Revert finished"
else
  jdtcore_file=$(findPlugin 'org.eclipse.jdt.core*MAXELER*.jar' '.')
  if [ $? -ne 0 ]; then
    echo $jdtcore_file
    exit 1
  fi
  jdtcore_bundle_version=$(getBundleVersion $jdtcore_file)
  if [ $? -ne 0 ]; then
    echo $jdtcore_bundle_version
    exit 1
  fi
  jdtui_file=$(findPlugin 'org.eclipse.jdt.ui*MAXELER*.jar' '.')
  if [ $? -ne 0 ]; then
    echo $jdtui_file
    exit 1
  fi
  jdtui_bundle_version=$(getBundleVersion $jdtui_file)
  if [ $? -ne 0 ]; then
    echo $jdtui_bundle_version
    exit 1
  fi

  findPluginEntry "org.eclipse.jdt.core" $jdtcore_bundle_version $config_file "org\.eclipse\.jdt\.core_.*\.jar"
  findPluginEntry "org.eclipse.jdt.ui" $jdtui_bundle_version $config_file "org\.eclipse\.jdt\.ui_.*\.jar"
  installPlugin "org.eclipse.jdt.core" $plugins_dir $config_file $jdtcore_bundle_version "plugins/$jdtcore_file"
  installPlugin "org.eclipse.jdt.ui" $plugins_dir $config_file $jdtui_bundle_version "plugins/$jdtui_file"
  echo "Installation successful"
fi

exit 0
