#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml"


function start {
  $DOCKER_COMPOSE_CMD up --build -d jore4-hastus
}

function stop_all {
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  $DOCKER_COMPOSE_CMD down
}

function usage {
  echo "
  Usage $0 <command>

  start
    Start hastus service in Docker container

  stop
    Stop all hastus Docker container

  remove
    Stop and remove hastus Docker container

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

  *)
    usage
    ;;
  esac
fi