#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

# initialize package folder
mkdir -p ./docker/update

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function check_docker {
  # based on https://github.com/HSLdevcom/jore4-flux#docker-compose

  if ! command -v gh; then
    echo "Please install the github gh tool on your machine."
    exit 1
  fi

  echo "Downloading latest version of E2E docker-compose package..."
  gh auth status || gh auth login

  # gh cannot overwrite existing files, therefore first download into separate dir. This way we still have the old copy
  # in case the download fails
  rm -rf ./docker/update/*
  gh release download e2e-docker-compose --repo HSLdevcom/jore4-flux --dir ./docker/update
  cp -R ./docker/update/* ./docker
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
