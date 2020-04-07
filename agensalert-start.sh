#!/bin/bash

target_path=./backend/target
jarfile=( "$target_path"/agens-alert*.jar )
cfgfile=( "$target_path"/*config.yml )
cfgname="${cfgfile%.*}"
echo $jarfile
[[ -e $jarfile ]] || {
  echo "ERROR: not exist agenspop jar file in backend/target/ \nTry build and start again.." >&2;
  exit 1;
  }

echo "Run target jar: $jarfile ($cfgname)"
# java -Xms2g -Xmx2g -jar $jarfile --spring.config.name=$cfgname
java -Xms1g -Xmx1g -jar $jarfile

