TOPDIR=$(dir $(lastword $(MAKEFILE_LIST)))
include $(TOPDIR)/Makefile.env.mk

PROJECT_NAME=$(shell realpath --relative-to="$(realpath $(TOPDIR))" "$(shell pwd)")

CONTAINER_TARGETS = container_tag container_push

# TAG ?= latest
TAG ?= dev
PROJECT_TAG_NAME = $(CONTAINER_REGISTRY)/$(ORG_NAME)/$(PROJECT_NAME)-kafka:$(TAG)

container: build_jar container_build_jvm $(CONTAINER_TARGETS)

container_native: build_native container_build_native $(CONTAINER_TARGETS)

dev:
	mvn compile quarkus:dev

mvn_install:
	mvn clean install -DskipTests

build_jar:
	mvn clean
	mvn package -DskipTests

build_native: 
	mvn package -Pnative -Dquarkus.native.container-build=true

container_build_jvm:
	$(CONTAINER_CTL) build -f src/main/docker/Dockerfile.jvm -t $(ORG_NAME)-$(PROJECT_NAME) .
	docker images | grep $(ORG_NAME)-$(PROJECT_NAME)

container_build_native:
	$(CONTAINER_CTL) build -f src/main/docker/Dockerfile.native -t $(ORG_NAME)-$(PROJECT_NAME) .
	docker images | grep $(ORG_NAME)-$(PROJECT_NAME)

container_tag:
	$(CONTAINER_CTL) tag $(ORG_NAME)-$(PROJECT_NAME) $(PROJECT_TAG_NAME)

container_push:
	$(CONTAINER_CTL) push $(PROJECT_TAG_NAME)

clean_deployment_bundle:
	rm -rf deployment_bundle

prepare_deployment_bundle:
	mkdir -p deployment_bundle

DEPLOYMENT_TEMPLATE=../template/bundle.yaml
ifneq (,$(wildcard ./template/bundle.yaml))
DEPLOYMENT_TEMPLATE=./template/bundle.yaml
endif

deployment_bundle: clean_deployment_bundle prepare_deployment_bundle
	cp $(DEPLOYMENT_TEMPLATE) deployment_bundle/$(PROJECT_NAME).yaml
	sed -i 's,$${CONTAINER_IMAGE},$(PROJECT_TAG_NAME),g' deployment_bundle/$(PROJECT_NAME).yaml
	sed -i 's,$${SERVICE_NAME},$(PROJECT_NAME),g' deployment_bundle/$(PROJECT_NAME).yaml
	sed -i 's,$${NAMESPACE},warehouse,g' deployment_bundle/$(PROJECT_NAME).yaml

.PHONY: clean_deployment_bundle prepare_deployment_bundle deployment_bundle container_build_jvm container_build_native $(CONTAINER_TARGETS) dev build_jar build_native container container_native
