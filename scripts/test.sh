#!/bin/bash

set -e

git clone -b eclipse-3.10.2-SNAPSHOT https://github.com/maxeler/plexus-compiler
mvn install -f plexus-compiler/pom.xml
mvn install -f plexus-compiler/plexus-compiler-api/pom.xml
mvn install -f plexus-compiler/plexus-compilers/pom.xml
mvn install -f plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml

mvn test -f examples/pom.xml
