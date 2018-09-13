#!/bin/bash

if ! [ -x "$(command -v aws)" ]; then
    echo 'Unable to find AWS CLI tools. Try `sudo apt install awscli`'
    exit -1
fi

printf "region: "
read region

printf "aws_key_id: "
read aws_key_id

printf "aws_secret: "
read aws_secret
printf "\n"

function deleteArchive() {
    archiveId=$1
    echo "Deleting archive $archiveId"

    response=`env AWS_ACCESS_KEY_ID=$aws_key_id AWS_SECRET_ACCESS_KEY=$aws_secret aws --region $region \
        glacier delete-archive --vault-name photos --archive-id $archiveId --account-id -`

    echo "delete-archive response (empty response == ok):"
    echo $response
    echo
}

for i in `cat delete-vault-content-ids.txt`; do
    deleteArchive "$i";
done;
