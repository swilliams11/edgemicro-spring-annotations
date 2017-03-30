# edgemicro-spring-annotations

### Summary
This repo contains the Edge Microgateway Spring annotations, which allows Spring developers
working in Cloud Foundry to spin up a Microgateway instance to protect their webservice.

Developers need an easier way to protect their services and annotations allow them to manage Edge Microgateway
from their code.

This project is currently WIP (work-in-process) so all features and functionality may not function correctly.


### Requirements
* Node.js must be installed globally.
* Edge Microgateway must be installed globally.
* User must have access to either Edge customer-managed cloud or Edge Google-managed installation.
* Must install Cloud Foundry meta-buildpack.
* Must install Cloud Foundry edgemicro-decorator-spring.
  * This installs Node.js and Edge Microgateway in the Cloud Foundry Diego container.
* Must have linux type environment; this will not work on Windows.


### Modules
This repo contains two modules.
1) edgemicro-spring-annotations-framework - contains the source code for the annotations.
2) edgemicro-spring-annotations-test - contains a preconfigured test of a customer
managed Edge installation.


### Usage Summary
The following annotations are included in the repo.
* `@EdgeMicro`
* `@EdgeMicroPrivateConfig`
* `@OAuth`
* `@Quota`
* `@SpikeArrest`


#### @EdgeMicro
This annotation is used to configure the customer-managed or Google-managed Edge installation.

##### Customer-managed
//TODO need to determine how to pass in a password from Environment Variable instead of via config
`@EdgeMicro(org = "demo", env = "prod", admin = "edgeadmin@email.com", password = "password", port = 8001)`

##### Google-managed
`@EdgeMicro(privateConfig = true, org = "demo", env = "prod", admin = "edgeadmin@email.com", password = "password", port = 8001)`

#### @EdgeMicroPrivateConfig
If the `@Edgemicro` annotation has `privateConfig=true`, then you must include the `@EdgeMicro`

`@EdgeMicroPrivateConfig(runtimeURL = "http://domainorip:9001", mgmtURL = "http://domainorip:8080")`

#### @OAuth
OAuth is enabled as shown below.

TODO remove the enabled = true/false, since it is duplicative.
@OAuth(allowNoAuthorization = true, enabled = false)


#### @SpikeArrest
* `timeUnit` can either be `minute` or `second`
* If `bufferSize` is not included then it is enabled by default.
@SpikeArrest(timeUnit = "minute", allow = 10)

@SpikeArrest(timeUnit = "minute", allow = 10, bufferSize = 50)


### Testing
Test cases will be located in the edgemicro-spring-annotations-framework module.

### TODO
* add Spring test cases