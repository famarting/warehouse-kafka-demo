CONTAINER_CTL = docker

DEV ?= false

ifeq ($(DEV), true)
CONTAINER_REGISTRY = 172.30.1.1:5000
ORG_NAME = warehouse
else
CONTAINER_REGISTRY = quay.io
ORG_NAME = famargon
endif