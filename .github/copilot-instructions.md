# OpenMUC Framework - AI Coding Instructions

## Project Overview

**OpenMUC** is a modular data collection and control framework built on **OSGi (Apache Felix)** architecture. It connects diverse communication devices and protocols, logs data, and provides web-based interfaces. Version: **0.20.0** | Java: **21** | Gradle: **8.5**

## Architecture Patterns

### OSGi Component Model

All bundles follow declarative OSGi service component architecture:
```java
@Component(service = DriverService.class)
public class MyDriver implements DriverService {
    @Reference
    private DataAccessService dataAccessService;  // Injected by OSGi
    
    @Activate
    void activate() { }
    
    @Deactivate
    void deactivate() { }
}
```

- Use `@Component(service = InterfaceType.class)` for service registration
- Use `@Reference` for service injection (NO @Autowired)
- Lifecycle methods: `@Activate` (bundle started), `@Deactivate` (bundle stopped)
- Never use traditional DI—OSGi manages everything

### Module Structure

```
projects/
├── core/              # Core APIs and DataManager (central hub)
│   ├── api/          # DataAccessService interface
│   ├── datamanager/  # DataManager service (orchestrates everything)
│   └── spi/          # Service provider interfaces for extensions
├── driver/           # Protocol drivers (Modbus, IEC60870, MQTT, etc.)
├── datalogger/       # Data persistence (SQL, ASCII, SLOTSDB, etc.)
├── server/           # Servers (Modbus TCP, IEC61850, REST)
├── lib/              # Shared libraries (MQTT, AMQP, OSGi utilities)
├── webui/            # Web UI components (React-based or custom)
└── app/              # Applications (SimpleDemoApp, BMSApp)
```

## Key Data Flows

### 1. Reading Data (Device → DataManager → Applications)
```
Driver.read() → Channel → DataManager.read(List<ReadRecordContainer>) 
→ RecordsReceivedListener.notify() → Applications
```

### 2. Writing Data (Applications → DataManager → Device)
```
Channel.write() → DataManager.write(List<WriteValueContainer>) 
→ Driver.write()
```

### 3. Data Logging
```
DataManager.read() → DataLoggerService.log(LoggingRecord[])
→ SQL/ASCII/SLOTSDB persistence
```

Key service: `DataAccessService` (implemented by `DataManager`)—use for all data access.

## Build & Deployment Workflow

### Prerequisites
- JDK 21 (required, set `JAVA_HOME`)
- Gradle wrapper handles 8.5 automatically

### Commands

**First-time setup:**
```bash
chmod +x setup_openmuc.sh
./setup_openmuc.sh                    # Downloads & extracts OSGi framework
./gradlew updateBundles -x test       # Copies all bundles to framework/bundle/
```

**Build a module:**
```bash
./gradlew :openmuc-driver-modbus:clean :openmuc-driver-modbus:build -x test
# Then copy JAR: cp build/libs-all/openmuc-driver-modbus.jar framework/bundle/
```

**Full rebuild:**
```bash
./gradlew clean build -x test         # Skips tests for speed
```

**Run OpenMUC:**
```bash
cd framework
./bin/openmuc start -fg               # Foreground (logs visible)
./bin/openmuc stop                    # Stop daemon
```

**Debug:**
```bash
cd framework
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
  -jar felix/felix.jar
# Then connect IntelliJ remote debugger to localhost:5005
```

## Common Patterns & Conventions

### 1. Data Types and Records
```java
// Record holds timestamp + typed value + Flag (quality/error indicator)
Record record = new Record(doubleValue, currentTimeMillis(), Flag.VALID);
channel.setRecord(record);

// Value types: DoubleValue, IntValue, LongValue, StringValue, etc.
// Always use Flag.VALID/INVALID/NO_VALUE for data quality
```

### 2. Logging Configuration
Drivers and dataloggers declare what data they consume via configuration:
```
xml
<channel id="device1.channel1">
    <address>MODBUS_ADDRESS:1</address>
    <loggingSettings>ascii_logger:1min;sql_logger:10min</loggingSettings>
</channel>
```
DataManager parses `loggingSettings` and routes data to appropriate loggers.

### 3. Datalogger Implementation
```java
@Component(service = DataLoggerService.class)
public class AsciiLogger implements DataLoggerService {
    @Override
    public void setChannelsToLog(List<LogChannel> channels) { }
    
    @Override
    public void log(LoggingRecord[] records) { }
    
    @Override
    public String getId() { return "ascii_logger"; }
}
```

### 4. Driver Implementation
```java
@Component(service = DriverService.class)
public class ModbusDriver implements DriverService {
    @Override
    public void connect() throws ConnectionException { }
    
    @Override
    public void read(List<ChannelRecordContainer> containers) 
            throws ConnectionException { }
    
    @Override
    public void write(List<ChannelValueContainer> containers) { }
}
```

## Project-Specific Conventions

- **Bundle naming:** `openmuc-{category}-{name}` (e.g., `openmuc-driver-modbus`)
- **Package naming:** `org.openmuc.framework.{category}.{name}`
- **Config files:** `framework/conf/properties/` (OSGi dynamic configuration)
- **Database:** Multiple backends supported (H2 embedded, PostgreSQL, MySQL)
- **Serialization:** JSON via Gson (v2.8.9—don't upgrade, packaging breaks OSGi)
- **Logging:** SLF4J + Logback (log at INFO level for user visibility)

## Testing & Integration

- **Unit tests:** JUnit 5 (`testImplementation` in build.gradle)
- **Test packages:** `src/test/java/` following source package structure
- **Integration tests:** Separate sourceSets `itest` for bundle integration
- **Run tests:** `./gradlew test` or `./gradlew :module:test`
- **Coverage:** Jacoco enabled; `./gradlew jacocoTestReport`

## Dependency Management

**Fixed versions in `configuration.gradle`:**
- JUnit: 5.11.0
- SLF4J API: 1.7.36
- Logback: 1.2.13
- Apache Felix: 7.0.5
- GSON: 2.8.9 (⚠️ don't upgrade)

**Embedding dependencies:** Use `configurations.embed` in JAR task for OSGI bundle inclusion.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `NoClassDefFoundError: com/fazecast/jSerialComm` | Run `./gradlew updateBundles` |
| `Unsupported class file major version 65` | Use JDK 21, set `JAVA_HOME` correctly |
| Felix cache corrupt | `rm -rf framework/felix-cache/*` then restart |
| Bundle not loading | Check `framework/bundle/` has `.jar` files; verify `@Component` annotation exists |

## Key Files to Study

- [DataAccessService API](projects/core/api/src/main/java/org/openmuc/framework/dataaccess/DataAccessService.java) — Main service interface
- [DataManager](projects/core/datamanager/src/main/java/org/openmuc/framework/core/datamanager/DataManager.java) — Orchestrator (large, ~1300 lines)
- [Modbus Driver](projects/driver/modbus/) — Example driver implementation
- [ASCII Datalogger](projects/datalogger/ascii/) — Example persistence layer
- [LatestValuesDao](projects/app/simpledemo/src/main/java/org/openmuc/framework/app/simpledemo/LatestValuesDao.java) — Sample app

## When Modifying Code

1. **Drivers:** Implement `DriverService`; handle connection lifecycle
2. **Dataloggers:** Implement `DataLoggerService`; respect `setChannelsToLog()`
3. **Core changes:** Verify no breaking changes to `DataAccessService`
4. **Dependencies:** Update in `configuration.gradle`, rebuild with `updateBundles`
5. **Bundles:** Always run `build` → `updateBundles` → test with `./bin/openmuc start -fg`
