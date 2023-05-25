#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

# initialize package folder
mkdir -p ./docker

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function download_docker_bundle {
  echo "Downloading latest version of E2E docker-compose package..."
  curl https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/download-docker-bundle.sh | bash
}

function start_all {
  download_docker_bundle
  $DOCKER_COMPOSE_CMD up --build -d jore4-hastus jore4-hasura jore4-testdb
}

function start_deps {
  download_docker_bundle
  $DOCKER_COMPOSE_CMD up --build -d jore4-hasura jore4-testdb
}

function stop_all {
  download_docker_bundle
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  download_docker_bundle
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
    Start Hastus service in Docker container

  start:deps
    Start only the Docker containers that are dependencies of Hastus service

  stop
    Stop all Hastus Docker container

  remove
    Stop and remove Hastus Docker container

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
    start_all
    ;;

  start:deps)
    start_deps
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
