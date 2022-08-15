# Project 72

Spring Cloud Sleuth & Zipkin

[https://gitorko.github.io/spring-cloud-sleuth-zipkin/](https://gitorko.github.io/spring-cloud-sleuth-zipkin/)

### Version

Check version

```bash
$java --version
openjdk 17.0.3 2022-04-19 LTS
```

### Zipkin

To run zipkin server use the docker command

```bash
docker run -d -p 9411:9411 --name my-zipkin openzipkin/zipkin
docker stop my-zipkin
docker start my-zipkin
```

Login to zipkin UI, wait for few seconds for server to be up.

[http://localhost:9411/zipkin/](http://localhost:9411/zipkin/)

### Dev

To run the code.

```bash
./gradlew clean build
./gradlew bootRun
```
