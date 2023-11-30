#!/usr/bin/env bash

set -euo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

# initialize package folder
mkdir -p ./docker

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function download_docker_bundle {
  # based on https://github.com/HSLdevcom/jore4-tools#download-docker-bundlesh

  echo "Downloading latest version of E2E docker-compose package..."
  curl https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/download-docker-bundle.sh | bash
}

function start_all {
  download_docker_bundle
  $DOCKER_COMPOSE_CMD up -d jore4-testdb jore4-hasura
  $DOCKER_COMPOSE_CMD up --build -d jore4-hastus
}

function start_deps {
  download_docker_bundle
  $DOCKER_COMPOSE_CMD up -d jore4-testdb jore4-hasura
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
    Build the Hastus service locally

  start
    Start Hastus service in Docker container

  start:deps
    Start only the Docker containers that are dependencies of the Hastus service

  stop
    Stop all Docker containers

  remove
    Stop and remove all Docker containers

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
  build)
    build
    ;;

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

  test)
    run_tests
    ;;

  help)
    usage
    ;;

  *)
    usage
    ;;
  esac
fi
