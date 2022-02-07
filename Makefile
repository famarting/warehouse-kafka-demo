TOPDIR=$(dir $(lastword $(MAKEFILE_LIST)))
include $(TOPDIR)/Makefile.env.mk

SERVICES = orders-service stocks-service warehouse-service

container: common $(SERVICES) 

build: common $(SERVICES)

publish: $(SERVICES)

common:
	$(MAKE) -C warehouse-common mvn_install

$(SERVICES): 
	$(MAKE) -C $@ $(MAKECMDGOALS)

clean_deployment_bundle:
	rm -rf deployment_bundle

prepare_deployment_bundle:
	mkdir -p deployment_bundle

deployment_bundle: $(SERVICES) copy_bundles
	
copy_bundles: clean_deployment_bundle prepare_deployment_bundle
	./scripts/copy_bundles.sh $(SERVICES)

ifeq ($(DEV), true)
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY) -u $(shell oc whoami) -p $(shell oc whoami -t)
else
login_container_registry:
	$(CONTAINER_CTL) login $(CONTAINER_REGISTRY)
endif

install-strimzi:
	wget https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.20.0/strimzi-cluster-operator-0.20.0.yaml
	oc new-project warehouse | true
	sed -i 's/namespace: .*/namespace: warehouse/' strimzi-cluster-operator-0.20.0.yaml
	kubectl apply -f strimzi-cluster-operator-0.20.0.yaml -n warehouse

release:
	cp deployment_bundle/* kubefiles/

.PHONY: login_container_registry clean_deployment_bundle prepare_deployment_bundle deployment_bundle copy_bundles container $(SERVICES)
