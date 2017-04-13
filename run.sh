#!/usr/bin/env bash

# 1: input_dir
# 2: input_months_range
# 3: aws_region
# 4: aws_glacier_vault_name

./install.sh
./build/install/backup-glacier-java/bin/backup-glacier-java $1 $2 3 4