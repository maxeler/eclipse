#!/usr/bin/env sh

cd ~
git clone -b eclipse-3.10.2-SNAPSHOT https://github.com/maxeler/plexus-compiler
mvn -f plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml install
