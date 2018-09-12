#!/usr/bin/env bash

./install.sh

printf "\n***************************************\n"
printf " Region set automatically to us-west-2\n"
printf " Valut set automatically to photos"
printf "\n***************************************\n\n"
./build/install/backup-glacier-java/bin/backup-glacier-java $@ -r us-west-2 -v photos 