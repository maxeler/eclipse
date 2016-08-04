#!/usr/bin/env sh

set -e

# Fetch submodules
git submodule update --init eclipse.platform.releng.aggregator
cd eclipse.platform.releng.aggregator
git submodule update --init eclipse.jdt.core
git submodule update --init eclipse.jdt.ui

# Build plugins
mvn -P build-individual-bundles clean install -f eclipse.jdt.core/pom.xml
mvn -P build-individual-bundles clean install -f eclipse.jdt.ui/pom.xml
