#!/bin/bash

RELEASE_DIR=maxeler-eclipse-plugin-$TRAVIS_TAG
VERSION=$TRAVIS_TAG
LINUX_IOS_INSTALL_SCRIPT=install-linux-mac.sh
WINDOWS_INSTALL_SCRIPT=install-windows.bat

build_installer () {
	javac $(find installer -name MaxelerECJInstaller.java)
	jar cvfe $RELEASE_DIR/Install.jar com.maxeler.eclipse.installer.MaxelerECJInstaller -C installer .
}

copy_plugin () {
  local plugin_name
  local plugin_build_dir
  plugin_name=$1
  plugin_build_dir=$2

  local plugin_name_variable=$(echo $plugin_name | sed "s|\.|_|g")
  local plugin_bundle_version=$(grep 'Bundle-Version: ' $plugin_build_dir/MANIFEST.MF | awk '{print $2}'i | tr -d '[[:space:]]')
  local plugin_release_name="$plugin_name-MAXELER-$VERSION"$"_$plugin_bundle_version.jar"

  cp $plugin_build_dir/$plugin_name-*SNAPSHOT.jar $RELEASE_DIR/plugins/$plugin_release_name
}

mkdir -p $RELEASE_DIR/plugins

copy_plugin "org.eclipse.jdt.core" "eclipse.platform.releng.aggregator/eclipse.jdt.core/org.eclipse.jdt.core/target"
copy_plugin "org.eclipse.jdt.ui" "eclipse.platform.releng.aggregator/eclipse.jdt.ui/org.eclipse.jdt.ui/target"

build_installer

export TRAVIS_TAG=$VERSION && ant -f eclipse.platform.releng.aggregator/eclipse.jdt.core/org.eclipse.jdt.core/scripts/export-ecj.xml

cp eclipse.platform.releng.aggregator/ecj-export/ecj-$VERSION.jar $RELEASE_DIR/

rm -rf eclipse.platform.releng.aggregator/ecj-export

cp scripts/$LINUX_IOS_INSTALL_SCRIPT $RELEASE_DIR/

tar czf $RELEASE_DIR.tar.gz $RELEASE_DIR

cp scripts/$WINDOWS_INSTALL_SCRIPT $RELEASE_DIR/

rm -r $RELEASE_DIR/$LINUX_IOS_INSTALL_SCRIPT

zip -r $RELEASE_DIR.zip $RELEASE_DIR 

rm -rf $RELEASE_DIR

exit 0
