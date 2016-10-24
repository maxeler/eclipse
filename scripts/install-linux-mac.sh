#!/bin/sh

usage_str="Usage: $0 <path-to-eclipse> [--revert]"
error_str="Error in arguments. Use command with -h or --help for help."

if [ $# -eq 0 ]; then
  java -jar Install.jar
  exit 0;
fi

if [ $# -eq 1 ]; then
  if [ "$1" = "-h" -o "$1" = "--help" ]; then
    echo $usage_str
    exit 1
  fi
  java -jar Install.jar $1
  exit 0;
fi

if [ $# -eq 2 ]; then
  if [ "$2" = "--revert" ]; then
    java -jar Install.jar $1 $2
    exit 0
  fi
  echo $error_str
  exit 1;
fi
