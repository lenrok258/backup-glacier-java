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

function initializeJob() {
    response=`env AWS_ACCESS_KEY_ID=$aws_key_id AWS_SECRET_ACCESS_KEY=$aws_secret aws --region $region \
        glacier initiate-job --vault-name photos --job-parameters '{"Type": "inventory-retrieval"}' --account-id -`

    echo "initiate-job response:"
    echo $response
    echo 

    jobId=`echo $response | sed -n 's/.*"jobId": "\(.*\)".*/\1/p'`

    echo "initiate-job jobId:"
    echo $jobId
    echo

    echo $jobId > list-vault-jobId.txt
}

function printJobResult() {
    echo "Waiting for the result..."

    jobId=`cat list-vault-jobId.txt`
    response=`env AWS_ACCESS_KEY_ID=$aws_key_id AWS_SECRET_ACCESS_KEY=$aws_secret aws --region $region \
        glacier get-job-output --vault-name photos --job-id $jobId --account-id - list-valut-result.txt 2>&1`

    echo "get-job-output response:"
    echo $response
    echo 

    echo "result:"
    cat list-valut-result.txt
}

if [ ! -f './list-vault-jobId.txt' ]; then
    echo "list-vault-jobId.txt does not exits. About to initialize a job...";
    initializeJob
fi;

printJobResult