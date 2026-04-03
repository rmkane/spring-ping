# Ping API — common tasks (requires Maven + JDK 21 on PATH)
MVN ?= mvn
SCRIPTS := ./scripts
# Matches <artifactId> and <version> in pom.xml; update if those change
JAR := target/ping-api-1.0.0-SNAPSHOT.jar

.PHONY: help compile test verify package build clean install dev run run-no-env run-jar request lint format

.DEFAULT_GOAL := help

compile: ## Compile main sources
	$(MVN) -q compile

lint: ## Check formatting + imports (Spotless; fails if sources need format)
	$(MVN) -q spotless:check

format: ## Apply Spotless (Eclipse formatter.xml + import order)
	$(MVN) -q spotless:apply

test: ## Run unit tests
	$(MVN) test

verify: ## Spotless check + tests (Maven verify)
	$(MVN) verify

package: ## Build Spring Boot executable JAR (skip tests)
	$(MVN) -q -DskipTests package

build: package ## Same as package

clean: ## Remove target/
	$(MVN) clean

install: ## Install artifact to local ~/.m2
	$(MVN) -q install -DskipTests

dev: ## Run app with .env loaded (DEBUG_HEADERS_ACCESS_TOKEN)
	$(SCRIPTS)/dev-server.sh

run: dev ## Alias for dev

run-no-env: ## Run Spring Boot without sourcing .env
	$(MVN) spring-boot:run

run-jar: package ## Run packaged JAR (export DEBUG_HEADERS_ACCESS_TOKEN if using debug API)
	java -jar $(JAR)

request: ## GET /api/debug/headers via curl (needs .env + running server)
	$(SCRIPTS)/request.sh

help: ## List targets
	@grep -E '^[a-zA-Z_-]+:.*?##' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-18s\033[0m %s\n", $$1, $$2}'
