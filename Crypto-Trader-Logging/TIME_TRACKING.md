# Time Tracking Integration Guide

This document explains everything a module needs to do to get execution time events flowing from that module into Crypto-Trader-Logging.

---

## How It Works

The time tracking pipeline has four stages:

```
@TimeTracked method
    → TimeTrackingAspect (AOP intercept)
        → LogEventsPublisher.publishExecutionSpeed(...)
            → EventPublisher.publish("executionSpeedLogs-out-0", payload)
                → Kafka topic: ct.logging.execution-speed.v1
                    → Crypto-Trader-Logging (ExecutionSpeedLogConsumerConfig)
```

All of the AOP, publishing, and auto-configuration infrastructure lives in the shared libraries. A consuming module only needs to wire it up correctly.

---

## Step 1 — Maven Dependencies

Add the following to your module's `pom.xml`:

```xml
<!-- Provides TimeTrackingAspect, CryptoTraderLoggingAutoConfig, LogEventsPublisher -->
<dependency>
    <groupId>org.cryptotrader</groupId>
    <artifactId>logging-config</artifactId>
</dependency>

<!-- Provides StreamBridge (required for EventPublisher to be created) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-kafka</artifactId>
</dependency>
```

`logging-config` transitively pulls in `logging-events` (provides
`LogEventsPublisher`, `ExecutionSpeedLogEventPayload`) and `universal-events`
(provides `EventPublisher`). You do not need to declare those separately in
Maven.

---

## Step 2 — Java Module System (`module-info.java`)

In a `module-info.java`, Maven transitive dependencies are **not**
automatically readable — you must declare each one explicitly.

Add the following `requires` to your `module-info.java`:

```java
requires org.cryptotrader.logging.library.config;   // TimeTrackingAspect, CryptoTraderLoggingAutoConfig
requires org.cryptotrader.logging.library.events;   // LogEventsPublisher, ExecutionSpeedLogEventPayload
requires org.cryptotrader.universal.library.events; // EventPublisher
requires spring.cloud.stream;                       // StreamBridge
```

Without `spring.cloud.stream`, the `@ConditionalOnBean(StreamBridge.class)`
check in `CryptoTraderLoggingAutoConfig` will silently fail and
`EventPublisher` will never be created. Without the other three, the types
themselves are inaccessible to the module system at runtime.

---

## Step 3 — Component Scan

`EventPublisher` lives in package `org.cryptotrader.universal.library.events`.
If your `@SpringBootApplication` class uses an
**explicit `@ComponentScan(basePackages = {...})`**, that overrides Spring
Boot's default scanning and `EventPublisher`'s `@Component` will never be
picked up.

Add the package to your scan list:

```java
@ComponentScan(basePackages = {
    "your.module.package",
    // ... other packages ...
    "org.cryptotrader.universal.library.events"   // required for EventPublisher
})
```

If your application class does **not** use an explicit `@ComponentScan`, Spring Boot's default scanning applies and this step is not needed.

---

## Step 4 — `application.yml` Configuration

Add the Kafka output binding for the execution speed log topic:

```yaml
spring:
  cloud:
    stream:
      defaultBinder: kafka
      bindings:
        executionSpeedLogs-out-0:
          destination: ct.logging.execution-speed.v1
          contentType: application/json
```

Do **not** add `output-bindings: executionSpeedLogs-out-0` or
`function.definition: executionSpeedLogs-out-0`. Those properties tell Spring
Cloud Function to look for a `Supplier`/`Function` bean with that name, which does not exist. The module uses `StreamBridge` (imperative, on-demand publishing), which resolves bindings by name from the `bindings:` map alone at call time.

---

## Step 5 — Annotate Methods with `@TimeTracked`

Import and apply the annotation to any method whose execution time you want tracked:

```java
import org.cryptotrader.universal.library.model.annotation.TimeTracked;

@TimeTracked
public void myMethod() { ... }

// With an expected duration threshold (milliseconds):
@TimeTracked(expectedMillis = 1000)
public void myTimeSensitiveMethod() { ... }

// To suppress the log line but still publish the event:
@TimeTracked(isLogged = false)
public void myQuietMethod() { ... }
```

### `@TimeTracked` Parameters

| Parameter        | Type      | Default | Description                                                                |
|------------------|-----------|---------|----------------------------------------------------------------------------|
| `isLogged`       | `boolean` | `true`  | Whether to emit a log line for this execution via `TimeTrackingAspect`     |
| `expectedMillis` | `long`    | `-1`    | Expected max duration in ms. `-1` means no threshold. Used in the payload. |
| `shouldPersist`  | `boolean` | `true`  | Whether to persist the event to the database.                              |

---

## Critical: AOP Self-Invocation Constraint

`TimeTrackingAspect` uses **Spring AOP** (proxy-based interception). A `@TimeTracked` method is only intercepted when called through a Spring proxy — i.e., when one Spring bean calls a method on a **different** Spring bean.

**Self-invocations are not intercepted:**

```java
// WRONG — self-invocation bypasses the proxy; @TimeTracked never fires
@Service
public class MyService {
    public void trigger() {
        this.doWork();          // direct call on 'this', not through proxy
    }

    @TimeTracked
    public void doWork() { ... }
}
```

**The fix — split into two beans:**

```java
// CORRECT — cross-bean call goes through the proxy
@Service
public class MyService {
    private final MyExecutionService myExecutionService;

    public void trigger() {
        myExecutionService.doWork();   // proxy intercepts → @TimeTracked fires
    }
}

@Service
public class MyExecutionService {
    @TimeTracked
    public void doWork() { ... }
}
```

Any method you want intercepted by `@TimeTracked` must live in a separate `@Service` (or `@Component`) bean from the class that calls it.

---

## LTW Setup

The following is already in place. If you are integrating a **new module**, replicate these steps.

> **`aop.xml` is not required per-module.** `Logging-Config` ships its own `META-INF/aop.xml` inside its JAR, which declares `TimeTrackingAspect` and scopes weaving to `org.cryptotrader..*`. The AspectJ weaver merges all `aop.xml` files found on the classpath, so any module that depends on `logging-config` automatically inherits this configuration.

### 1. Maven dependencies (`pom.xml`)

```xml
<!-- AspectJ weaving support -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
<!-- Spring LTW agent (provided scope — used as -javaagent only) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-instrument</artifactId>
    <scope>provided</scope>
</dependency>
```

### 2. Maven plugins (`pom.xml`)

Add the `spring-boot-maven-plugin` with the agent in `jvmArguments`, the `maven-dependency-plugin` to copy the agent jar for tests, and update the Surefire `argLine`:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>your.module.YourApplication</mainClass>
        <jvmArguments>-javaagent:${settings.localRepository}/org/springframework/spring-instrument/${spring-framework.version}/spring-instrument-${spring-framework.version}.jar</jvmArguments>
    </configuration>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-spring-instrument</id>
            <phase>process-test-classes</phase>
            <goals><goal>copy</goal></goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>org.springframework</groupId>
                        <artifactId>spring-instrument</artifactId>
                        <outputDirectory>${project.build.directory}/agents</outputDirectory>
                        <destFileName>spring-instrument.jar</destFileName>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>@{argLine} -Dfile.encoding=UTF-8 -XX:+EnableDynamicAgentLoading -javaagent:${project.build.directory}/agents/spring-instrument.jar</argLine>
    </configuration>
</plugin>
```

### 3. Enable LTW in your application class

```java
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@SpringBootApplication
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
public class YourApplication { ... }
```

---

## Checklist

- [ ] `logging-config` declared in `pom.xml`
- [ ] `spring-cloud-stream` and `spring-cloud-starter-stream-kafka` declared in `pom.xml`
- [ ] `spring-aspects` and `spring-instrument` declared in `pom.xml`
- [ ] `spring-boot-maven-plugin` configured with `-javaagent` in `jvmArguments`
- [ ] `maven-dependency-plugin` copies `spring-instrument.jar` to `target/agents`
- [ ] `maven-surefire-plugin` `argLine` includes `-javaagent:${project.build.directory}/agents/spring-instrument.jar`
- [ ] `@EnableLoadTimeWeaving(aspectjWeaving = ENABLED)` on your application class
- [ ] If `module-info.java` exists: `requires` added for `logging.library.config`, `logging.library.events`, `universal.library.events`, and `spring.cloud.stream`
- [ ] If explicit `@ComponentScan` is used: `org.cryptotrader.universal.library.events` added to `basePackages`
- [ ] `executionSpeedLogs-out-0` binding declared in `application.yml` pointing to `ct.logging.execution-speed.v1`
- [ ] No `output-bindings` or `function.definition` set for `executionSpeedLogs-out-0`
- [ ] `@TimeTracked` methods live in a separate `@Service` bean from their callers (two-bean pattern)
