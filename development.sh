#!/usr/bin/env bash

set -euo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

DOCKER_COMPOSE_CMD="docker compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

# utility to manipulate YAML files
function exec_yq() {
  # Run yq locally if it exists in the path; otherwise, run yq in Docker
  # container.
  if ! command -v yq &> /dev/null
  then
    docker run --rm -v "${WD}/docker":/docker -w / mikefarah/yq -i -e "$@"
  else
    yq -i -e "$@"
  fi
}

function download_docker_compose_bundle {
  # based on https://github.com/HSLdevcom/jore4-tools#download-docker-bundlesh

  echo "Downloading latest version of E2E docker-compose package..."
  curl https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/download-docker-bundle.sh | bash

  # Remove port number for database service definition because the port number
  # is overridden in the custom configuration and we don't want to bind two
  # ports (as this is multi-valued property in docker-compose).
  echo "Removing database port number from docker-compose base file..."
  exec_yq 'del(.services.jore4-testdb.ports)' ./docker/docker-compose.yml

  # Remove port number for Hasura service definition because the port number
  # is overridden in the custom configuration and we don't want to bind two
  # ports (as this is multi-valued property in docker-compose).
  echo "Removing Hasura port number from docker-compose base file..."
  exec_yq 'del(.services.jore4-hasura.ports)' ./docker/docker-compose.yml

  # Remove database hostname for Hasura service definition because the hostname
  # is overridden in the custom configuration and we don't want to bind two
  # values (as this is multi-valued property in docker-compose).
  echo "Removing Hasura hostname from docker-compose base file..."
  exec_yq 'del(.services.jore4-hasura.secrets.[] | select(.source == "hasura-db-hostname"))' ./docker/docker-compose.yml

  echo "Finished removing properties to be overridden from docker-compose base file."
}

prepare_timetables_data_inserter() {
  ensure_hasura_submodule_initialized

  cd jore4-hasura/test/hasura
  yarn install
  yarn timetables-data-inserter:build
  cd -
}

ensure_hasura_submodule_initialized() {
  if [ ! -d jore4-hasura/test ]; then
    echo "jore4-hasura submodule not found! Initializing..."

    git submodule init
    git submodule update
    echo "jore4-hasura submodule: setting sparse checkout..."
    cd jore4-hasura
    git sparse-checkout init --cone
    git sparse-checkout set test/hasura
    cd -

    echo "jore4-hasura submodule initialized."
  fi

  echo "jore4-hasura submodule: updating..."
  git submodule update
  echo "jore4-hasura submodule up to date."
}

# jore4-testdb - Jore 4 database. This is the database used when starting the application.
# jore4-testdb-test - Jore 4 database instance for the integration tests.
# jore4-hasura - Hasura. We have to start Hasura because it ensures that db migrations are run to the Jore 4 database.
# jore4-hasura-test - Hasura instance used in the integration tests to run db migrations to the Jore 4 database.

function start_all {
  $DOCKER_COMPOSE_CMD up -d jore4-testdb jore4-hasura jore4-testdb-test jore4-hasura-test jore4-tiamat jore4-tiamat-test
  $DOCKER_COMPOSE_CMD up --build -d jore4-hastus
}

function start_deps {
  $DOCKER_COMPOSE_CMD up -d jore4-testdb jore4-hasura jore4-testdb-test jore4-hasura-test jore4-tiamat jore4-tiamat-test
}

function stop_all {
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  $DOCKER_COMPOSE_CMD down
}

function build {
  mvn install
}

function run_tests {
  mvn test
}

function print_usage {
  echo "
  Usage: $(basename "$0") <command>

  build
    Build the Hastus service locally.

  build:data-inserter
    Build the Data-Inserter for integration tests (Git submodule).

  start
    Start Hastus service in Docker container.

  start:deps
    Start only the Docker containers that are dependencies of the Hastus
    service.

  stop
    Stop all Docker containers.

  remove
    Stop and remove all Docker containers.

  test
    Run tests locally.

  help
    Show this usage information.
  "
}

COMMAND=${1:-}

if [[ -z $COMMAND ]]; then
  print_usage
  exit 1
fi

case $COMMAND in
build)
  build
  ;;

build:data-inserter)
  prepare_timetables_data_inserter
  ;;

start)
  download_docker_compose_bundle
  start_all
  prepare_timetables_data_inserter
  ;;

start:deps)
  download_docker_compose_bundle
  start_deps
  prepare_timetables_data_inserter
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
  print_usage
  ;;

*)
  echo ""
  echo "Unknown command: '${COMMAND}'"
  print_usage
  exit 1
  ;;
esac
