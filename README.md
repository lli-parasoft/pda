# Parasoft Demo Application
The Parasoft Demo Application is an example Spring Boot project. The application is configurable and customizable, and is used to demonstrate functionality in a variety of Parasoft tools.

## Getting Started
### Building .war from sources
Once you download the sources, build the project as a .war file using the Gradle wrapper.

In Linux / Cygwin:
```
./gradlew bootWar
```
In Windows:
```
gradlew.bat bootWar
```
The file parasoft-demo-app-1.1.0.war can be found in build/libs after building.
### Running
You can run the application either directly from sources,

In Linux / Cygwin:
```
./gradlew bootRun
```
In Windows:
```
gradlew.bat bootRun
```
Or as a .war file with Java (after building):
```
java -jar build/libs/parasoft-demo-app-1.1.0.war
```
### Importing into your IDE
If you want to import the project into your IDE, be sure to do the following:
1. Import the project as a Gradle project. You may need to synchronize or refresh the project after importing.
2. Install a Lombok plugin for your IDE since the project uses Lombok.
#### Changing server port
When launching the app, you can specify the port to use with a command like the following:
```
./gradlew bootRun -Pport=8888
```
## Using the Demo Application
Once started, you can access the application at [http://localhost:8080](http://localhost:8080).

Login with one of these users:
- Username `purchaser` password `password`
- Username `approver` password `password`

## Connect to embedded HSQLDB server instance
There are four databases (one for global and three for industries) in Parasoft Demo Application, which are **global**, **outdoor**, **defense** and **aerospace**.

| Database name | Description                                          |
|---------------|------------------------------------------------------|
| global        | Used to store the user, role and configuration data. |
| outdoor       | Used to store the data about outdoor industry.       |
| defense       | Used to store the data about defense industry.       |
| aerospace     | Used to store the data about aerospace industry.     |

### Connection configuration
Parasoft Demo Application exposes port 9001 for the user to connect to the HSQLDB database remotely.

- Global database

| Option   | Value                                      |
|----------|--------------------------------------------|
| Driver   | `org.hsqldb.jdbcDriver`                    |
| URL      | `jdbc:hsqldb:hsql://localhost:9001/global` |
| Username | `SA`                                       |
| Password | `pass`                                     |

- Industry database

| Option   | Value                                               |
|----------|-----------------------------------------------------|
| Driver   | `org.hsqldb.jdbcDriver`                             |
| URL      | `jdbc:hsqldb:hsql://localhost:9001/{database name}` |
| Username | `SA`                                                |
| Password | `pass`                                              |

## Using Parasoft JMS Proxy and Virtual Asset
There are two main services for order management in PDA, **order service** and **inventory service**. After an order is submitted, order service sends
a request through messaging queue to check and decrease the inventory. After the operation is done, inventory service sends a response through messaging queue which includes the information of the operation result.

**Configuration details for embedded ActiveMQ server**

| Option                            | Value                                                    |
|-----------------------------------|----------------------------------------------------------|
| Provider URL                      | `tcp://localhost:61626`                                  |
| Initial context class             | `org.apache.activemq.jndi.ActiveMQInitialContextFactory` |
| Connection factory                | `ConnectionFactory`                                      |
| Username                          | `admin`                                                  |
| Password                          | `admin`                                                  |
| Inventory service request queue   | `inventory.request`                                      |
| Inventory service response queue  | `inventory.response`                                     |


PDA uses two default queues to support messaging between **order service** and **inventory service**.
The configuration for queues can be changed or reset to default on PDA Demo Administration page.

<img src="src/main/resources/static/common/images/mq_default_mode_diagram.png" alt="mq default mode diagram">

### Using JMS Proxy
To use the queueing system with JMS proxy, you can change **Destination queue** and **Reply to queue** to customized queue names.
The **Client Connection** in message proxy should be configured with the two customized queues.
The **Server Connection** in message proxy should be configured with the two default queues.

<img src="src/main/resources/static/common/images/mq_proxy_mode_diagram.png" alt="mq proxy mode diagram">

### Using virtual asset
To use the queueing system with virtual asset, you can change **Destination queue** to a customized destination queue name.
The virtual asset deployment should be configured to listen to the customized destination queue and reply to the default response queue.

<img src="src/main/resources/static/common/images/mq_virtual_asset_mode_diagram.png" alt="mq virtual asset mode diagram">

## Using Kafka and Virtual Asset
Kafka is an **optional** MQ in PDA.

**Configuration details for external Kafka server**

|    Option   |             Value              |
|-------------|--------------------------------|
| Broker URL  |       `localhost:9092`         |
| Group ID    |       `inventory-operation`    |

PDA uses two default topics to support messaging between order service and inventory service. The configuration for topics can be changed or reset to default on PDA Demo Administration page.

<img src="src/main/resources/static/common/images/Kafka_default_mode_diagram.png" alt="Kafka default mode diagram">

### How to connect to external Kafka
To use Kafka in PDA, you will need to finish following steps:

1. Start a Kafka server
2. Set Kafka host and consumer group ID in application.properties file.
3. Start PDA and go to parasoft queue configuration section in PDA Demo Administration page. Change **MQ Type** to Kafka, Click **Kafka configuration details** link then you can click Test Connection to verify the connection with Kafka.
4. If you successfully connect to Kafka, click save button to save configuration.

If Kafka server is down when PDA is using Kafka mode, there will be a warning sign on settings button. Please check your Kafka server when the warning sign appear;

### Using virtual asset
To use Kafka with virtual asset, you can change Inventory service request topic
to a customized request topic name. The virtual asset deployment should be configured to listen to the customized request topic and produce messages to the default response topic.

<img src="src/main/resources/static/common/images/Kafka_virtual_asset_mode_diagram.png" alt="Kafka virtual asset mode diagram">


## Using Parasoft JDBC Proxy
1. Find the **ParasoftJDBCDriver.jar** in **{SOAtest & Virtualize installation directory}/{version}/proxies**.
2. Copy it to **{root directory of parasoft-demo-app}/lib**. (Create the folder if it does not already exist.)
3. Open **SOAtest & Virtualize** desktop, add the **ParasoftJDBCDriver.jar** to **Parasoft > Preferences > JDBC Drivers**.
4. Start Virtualize server in **Virtualize Server** view.
5. Enable the **PARASOFT JDBC PROXY** in PDA **Demo Administration** page, modify started server's **URL**, **Parasoft Virtualize Server path**, and **Parasoft Virtualize group ID** if necessary.
6. Go to **SOAtest & Virtualize** desktop and refresh the Server. If the **Parasoft JDBC Proxy** is enabled successfully, there will be a controller which has the same name as group ID under **JDBC Controllers**.
7. Change the settings of the controller.

## Using SOAtest DB Tool
1. Open **SOAtest & Virtualize** desktop, add the hsqldb driver to **Parasoft > Preferences > JDBC Drivers**.
2. Create a tst file with **DB Tool**.
3. Open the **DB Tool** and open the **Connection** tab. Select **Local** option and fill in **Driver**, **URL**, **Username**, and **Password** for the database.

| Option   | Value                                               |
|----------|-----------------------------------------------------|
| Driver   | `org.hsqldb.jdbcDriver`                             |
| URL      | `jdbc:hsqldb:hsql://localhost:9001/{database name}` |
| Username | `SA`                                                |
| Password | `pass`                                              |

4. Write SQL statement in **SQL Query** tab and run the test. The query results will be shown in **Traffic Object**.