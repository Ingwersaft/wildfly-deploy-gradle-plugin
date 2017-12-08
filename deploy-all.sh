#!/usr/bin/env bash
#ARG MGMT_USER=jboss
#ENV MGMT_USER ${MGMT_USER}
#
#ARG MGMT_PASS=root1234
#ENV MGMT_PASS ${MGMT_PASS}
if [ $# -ne 6 ]; then
	echo "usage: ./deploy-all.sh <WILDFLY_USERNAME> <WILDFLY_PW> <WILDFLY_HOST> <WILDFLY_PORT> <ARTIFACTORY_USERNAME> <ARTIFACTORY_PW>"
	echo ""
	echo "also artifacts-latest must end with an empty line..."
	exit 2
fi
WILDFLY_USERNAME=$1
WILDFLY_PW=$2
WILDFLY_HOST=$3
WILDFLY_PORT=$4

ARTIFACTORY_USERNAME=$5
ARTIFACTORY_PW=$6

echo "deleting old wars"
rm *.war
echo "downloading wars:"

while read line
do
    echo "handling line: $line"
	IFS='#' read -r -a array <<< "$line"
	GROUPID=${array[0]}
	GROUPID=$(echo $GROUPID | sed 's#[.]#/#g')
	ARTIFACTID=${array[1]}
	VERSION=${array[2]}
	TYPE=${array[3]}
	FILENAME=${array[4]}

	SUBPATH="/$GROUPID/$ARTIFACTID/$VERSION/$ARTIFACTID-$VERSION.$TYPE"
	echo "going to download $SUBPATH and name it $FILENAME"

	echo ""

       // wget

	if [ $? -ne 0 ]; then
        	echo "error with $ARTIFACTID-$VERSION.$TYPE"
		exit 1
    	fi

    echo "downloading done -> deploying"
    java -jar jboss-cli-client.jar -c controller=${WILDFLY_HOST}:${WILDFLY_PORT} --user=${WILDFLY_USERNAME} --password=${WILDFLY_PW} \
        --command="deploy $FILENAME --force"
	echo "deployment done"
	echo ""
done < artifacts-latest

echo "deployment info:"
java -jar jboss-cli-client.jar -c controller=${WILDFLY_HOST}:${WILDFLY_PORT} --user=${WILDFLY_USERNAME} --password=${WILDFLY_PW} \
    --command="deployment-info"

echo "deleting wars again"
rm *.war
