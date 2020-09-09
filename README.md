# Warehouse example application with Kafka and Quarkus

This repository contains an example application for orders processing in a warehouse, with the purpose of trying and showing different technologies. This is a diagram showcasing in high level how the application works

![app diagram](/img/diagram.png)

## How it's implemented?

This example microservices application is implemented combining [quarkus] and [strimzi]. Quarkus is a Java framework used to code the microservices and Strimzi is the best kubernetes operator to manage kafka deployments

## How to run it?

The only prerequisite is having an openshift cluster with strimzi installed.
You can install strimzi with
```bash
make install-strimzi
```

### Package the application
```bash
make login_container_registry
oc new-project warehouse
make container
make deployment_bundle
```

### Deploy the application

```bash
oc apply -f kubefiles
```
You can check the application is running with
```bash
oc get pod
```
### Quick demo after deploying

```bash
./scripts/scripts-demo/add_stock.sh
./scripts/scripts-demo/send_orders.sh
./scripts/scripts-demo/watch_orders.sh
```

[quarkus]: <https://quarkus.io/>
[strimzi]: <https://strimzi.io/>