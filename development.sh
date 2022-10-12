#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

# initialize package folder
mkdir -p ./docker

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function check_docker {
  # compare versions
  GITHUB_VERSION=$(curl -L https://github.com/HSLdevcom/jore4-flux/releases/download/e2e-docker-compose/RELEASE_VERSION.txt --silent)
  LOCAL_VERSION=$(cat ./docker/RELEASE_VERSION.txt || echo "unknown")

  # download latest version of the docker-compose package in case it has changed
  if [ "$GITHUB_VERSION" != "$LOCAL_VERSION" ]; then
    echo "E2E docker-compose package is not up to date, downloading a new version."
    curl -L https://github.com/HSLdevcom/jore4-flux/releases/download/e2e-docker-compose/e2e-docker-compose.tar.gz --silent | tar -xzf - -C ./docker/
  else
    echo "E2E docker-compose package is up to date, no need to download new version."
  fi
}

function start {
  check_docker
  $DOCKER_COMPOSE_CMD up --build -d jore4-hastus
}

function stop_all {
  check_docker
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  check_docker
  $DOCKER_COMPOSE_CMD down
}

function build {
  mvn install
}

function run_tests {
  mvn test
}

function usage {
  echo "
  Usage $0 <command>

  build
    Build the project locally

  start
    Start hastus service in Docker container

  stop
    Stop all hastus Docker container

  remove
    Stop and remove hastus Docker container

  test
    Run tests locally

  help
    Show this usage information
  "
}

if [[ -z ${1} ]]; then
  usage
else
  case $1 in
  start)
    start
    ;;

  stop)
    stop_all
    ;;

  remove)
    remove_all
    ;;

  help)
    usage
    ;;

  build)
    build
    ;;

  test)
    run_tests
    ;;

  *)
    usage
    ;;
  esac
fi
