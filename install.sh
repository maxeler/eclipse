#!/bin/bash -e
BASE=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

function bundle_version()
{
	local manifest="$1"
	grep -e "Bundle-Version: " "$manifest" | sed -r "s/Bundle-Version: //" | tr -d '\n' | tr -d '\r'
}

if [ "$#" -ne 1 ]
then
	echo "$0 eclipse-dir"
	exit 1
else
	cd $BASE

	ECLIPSE_DIR="$1"
	BUNDLE_INFO="$ECLIPSE_DIR/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"
	BUNDLE_INFO_BAK=$BUNDLE_INFO.bak

	echo "[Compiling]"
	mvn -f eclipse.jdt.core/org.eclipse.jdt.core/pom.xml -P build-individual-bundles package
	mvn -f eclipse.jdt.ui/org.eclipse.jdt.ui/pom.xml -P build-individual-bundles package

	echo "[Installing]"
	CORE_JAR=`ls eclipse.jdt.core/org.eclipse.jdt.core/target/org.eclipse.jdt.core-*-SNAPSHOT.jar`
	UI_JAR=`ls eclipse.jdt.ui/org.eclipse.jdt.ui/target/org.eclipse.jdt.ui-*-SNAPSHOT.jar`

	cp $CORE_JAR $ECLIPSE_DIR/plugins
	cp $UI_JAR $ECLIPSE_DIR/plugins

	echo "[Patching]"
	CORE_VERSION=`bundle_version "./eclipse.jdt.core/org.eclipse.jdt.core/target/MANIFEST.MF"`
	UI_VERSION=`bundle_version "./eclipse.jdt.ui/org.eclipse.jdt.ui/target/MANIFEST.MF"`
	
	if [ -e "$BUNDLE_INFO_BAK" ]
	then
		echo "Error: Backup file '$BUNDLE_INFO_BAK' already exists and would be overwritten. Either move it to a safe place or delete it to continue installation."
		echo 1
	else
		sed -r \
		    -e "s|org\.eclipse\.jdt\.core,.*+|org.eclipse.jdt.core,$CORE_VERSION,plugins/$(basename $CORE_JAR),4,false|" \
		    -e "s|org\.eclipse\.jdt\.ui,.*+|org.eclipse.jdt.ui,$UI_VERSION,plugins/$(basename $UI_JAR),4,false|" \
		    -i.bak \
		    "$BUNDLE_INFO"
	fi
fi
