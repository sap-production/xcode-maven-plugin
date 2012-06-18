#!/bin/sh

die() {
    echo "$*" >&2
    exit 1
}


RELEASE_VERSION=$1
DEVELOPMENT_VERSION=$2
TAG=$3

if [ "$RELEASE_VERSION" = "" ]
  then
    die "Release version was not provided"
fi

if [ "$DEVELOPMENT_VERSION" = "" ]
  then
    die "Next development version was not provided"
fi

if [ "$TAG" == "" ]
  then
    die "No tag has been provided"
fi

echo release version: $RELEASE_VERSION
echo next development version: $DEVELOPMENT_VERSION

cd ../..
git fetch && git checkout master && git rebase origin/master && git clean -fdx || die "Could not checkout project from git."
mvn clean -P continuous-integration || die "Maven clean did not succeed."

mvn --batch-mode -P continuous-integration org.apache.maven.plugins:maven-release-plugin:2.2.1:clean
mvn --batch-mode -P continuous-integration org.apache.maven.plugins:maven-release-plugin:2.2.1:prepare -Dtag=${TAG} -DreleaseVersion=${RELEASE_VERSION} -D developmentVersion=${DEVELOPMENT_VERSION} -DpreparationGoals="clean install"
