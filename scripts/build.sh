#!/usr/bin/env sh

mvn -P build-individual-bundles install -f eclipse-platform-parent/pom.xml
mvn -P build-individual-bundles install -f eclipse.jdt.core/pom.xml
mvn -P build-individual-bundles install -f eclipse.jdt.core/org.eclipse.jdt.core/pom.xml

git clone -b eclipse-3.10.2-SNAPSHOT https://github.com/maxeler/plexus-compiler
mvn install -f plexus-compiler/pom.xml
mvn install -f plexus-compiler/plexus-compiler-api/pom.xml
mvn install -f plexus-compiler/plexus-compilers/pom.xml
mvn install -f plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml

mvn -P build-individual-bundles install -f eclipse.jdt.ui/org.eclipse.jdt.ui/pom.xml

mvn test -f examples/pom.xml
