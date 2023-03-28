# LeapFramework

As all are mavenized , maven commands as below would be used for all the given projects:
1.	Mvn clean
2.	Mvn install (will not skip testCases) / mvn install –DskipTests
3.	Mvn eclipse:clean
4.	Mvn eclipse:eclipse
NB: Verify the build-path for a valid addition of .jar files respective for the above projects.
A hierarchy of project setup has to be followed which will be as follows: 
leap-DdlUtils-jdbc, leap-Metamodel-jdbc, leap-framework, leap-core, custom-feature & feature-installer.


Question and Answers:
======================
*********************************************************************************************************************************************************************************************
Database setup for leap framework:-
===================================
You will find the Database-dump folder in repo import that file required based on database type.

Single Properties File For all Framework Congifuration GLOBAL_APP_DEPLOYMENT_CONFIG :
===================================================================================
We have \leap-framework\src\main\resources\globalAppDeploymentConfig.properties for configuration of all the framework components and also for database configuration.If you have cassandra as database than we have to configure cassandraDBConfig.properties based on the devlopment environment.

EventDispatcherTracker Table :
=============================
We require this table setup in leap database or the database scheam which you are using.This table is required to keep the track of event generated on each request.
Only thing you need to do is create the table in mysql db(leap)-->

CREATE TABLE `eventdispatchtracker` (
  `tenantId` varchar(50) NOT NULL DEFAULT '',
  `siteId` varchar(50) NOT NULL DEFAULT '',
  `leapEventId` varchar(50) NOT NULL,
  `requestId` varchar(100) NOT NULL DEFAULT '',
  `leapEvent` longtext,
  `eventStoreId` varchar(50) NOT NULL,
  `eventCreatedDTM` timestamp NULL DEFAULT NULL,
  `lastFailureDTM` timestamp NULL DEFAULT NULL,
  `status` varchar(25) NOT NULL,
  `failureReason` varchar(20000) DEFAULT NULL,
  `retryCount` int(11) DEFAULT NULL,
  PRIMARY KEY (`tenantId`,`siteId`,`leapEventId`,`requestId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

EventSubscriptionTracker Table :
=============================
We require this table setup in leap database or the database scheam which you are using.This table is required to keep the track of event generated on each request.
Only thing you need to do is create the table in mysql db(leap)-->


CREATE TABLE `EventSubscriptionTracker` (
  `tenantId` varchar(100) DEFAULT NULL,
  `siteId` varchar(100) DEFAULT NULL,
  `subscriptionId` varchar(100) DEFAULT NULL,
  `topic` varchar(45) DEFAULT NULL,
  `topicPartition` varchar(50) DEFAULT NULL,
  `topicOffset` varchar(50) DEFAULT NULL,
  `eventData` varchar(15000) DEFAULT NULL,
  `eventFetchedDTM` timestamp NULL DEFAULT NULL,
  `lastfailureDTM` timestamp NULL DEFAULT NULL,
  `failureMsg` varchar(500) DEFAULT NULL,
  `status` varchar(45) DEFAULT NULL,
  `isRetryable` tinyint(1) DEFAULT NULL,
  `retryCount` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

Note :- Feature installer will create the table if does't exist already but the timestamp COLUMN should use the default value as NULL

*****************************************************************************************************************************************************************************************
Why we have Quartz dependency?
==================================
For retrying the failed events.Quartz component of camel is been used for retrying at the specific interval to perform retry operation on failed events.

***************************************************************************************************************************************************************************************
Do we need to create setup for QUARTZ database for performing retry ?
=====================================================================
No you don't require if you are running your feature on single instance and not in cluster.If you are running the feature on the single instance than you can configure following properties in
/leap-core/src/main/resources/quartz.properties  :-

* org.quartz.scheduler.instanceId: AUTO
* org.quartz.scheduler.skipUpdateCheck: true
* org.quartz.threadPool.class: org.quartz.simpl.SimpleThreadPool
* org.quartz.threadPool.threadCount: 1
* org.quartz.threadPool.threadPriority: 5
* org.quartz.jobStore.misfireThreshold: 60000
* org.quartz.jobStore.class: org.quartz.simpl.RAMJobStore

But if you want to run feature in cluster and you want retry of eventing to work in cluster than you need to have the database setup for Quartz as follow


* org.quartz.scheduler.instanceId: AUTO
* org.quartz.scheduler.skipUpdateCheck: true
* org.quartz.threadPool.class: org.quartz.simpl.SimpleThreadPool
* org.quartz.threadPool.threadCount: 1
* org.quartz.threadPool.threadPriority: 5
* org.quartz.jobStore.misfireThreshold: 60000
* org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
* org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.StdJDBCDelegate
* org.quartz.jobStore.useProperties: false
* org.quartz.jobStore.dataSource: myDS
* org.quartz.jobStore.tablePrefix: QRTZ_
* org.quartz.jobStore.isClustered: true
* org.quartz.dataSource.myDS.driver: com.mysql.jdbc.Driver
* org.quartz.dataSource.myDS.URL: jdbc:mysql://localhost:3306/Quartz
* org.quartz.dataSource.myDS.user: root
* org.quartz.dataSource.myDS.password: root
* org.quartz.dataSource.myDS.maxConnections: 5

Reason :- 
*******
Each node in a Quartz cluster is a separate Quartz application that is managed independently of the other nodes. This means that you must start and stop each node individually.
Unlike clustering in many application servers, the separate Quartz nodes do not communicate with one another or with an administration node. 
Because clustered nodes rely on the database to communicate the state of a Scheduler instance, you can use Quartz clustering only when using a JDBC JobStore. 
you can't use RAMJobStore with clustering.Instead, the Quartz applications are made aware of one another through the database tables.
(Future versions of Quartz will be designed so that nodes communicate with one another directly rather than through the database.) 


*****************************************************************************************************************************************************************************************
If your feature belong to tenant and site as 'all' than what you need to configure?
=======================================================================================
Mention the featureGroup and featureName in  \leap-framework\src\main\resources\globalAppDeploymentConfig.properties with ',' as sperator if there are multiple features.(featureGroup-featureName)
e.g:-  rootDeployableFeatures=authentication-authenticationservice,authorization-authorizationservice


##### Release Process :- 
**********************
- Before Releasing either the Beta or Stable Release, we have to check if any changes has done in leap-DdlUtils-jdbc, leap-Metamodel-jdbc, leap-framework, leap-core

- Once Verified the changes build the leap-core project using the command prompt "mvn clean install" Please dont mention -DskipTests while building,

- After Build Got sucessful, Go to the target folder of the leap-core project, and open the command terminal and deploy the jar file as a package to github repository using    the below command
  
  ### mvn deploy:deploy-file -DartifactId="leap-core-starter" -DgroupId="com.attunedlabs" -Dpackaging="jar" -Dversion="1.0.2-SNAPSHOT" -Dfile="leap-core-0.0.1-SNAPSHOT-shaded.jar" -DrepositoryId="github" -Durl="https://maven.pkg.github.com/attunedlabs/LeapFramework-rwx"

- Before running the command you need to configure in Settings.xml file of Maven to enable access to the repo. The password included in this 
  doc only allows for reading (download) of the mvn package. Anyone that needs to be able to publish a package should talk with client to get 
  your own "write" key.
 
 ``` xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
        <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/attunedlabs/LeapFramework-rwx</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <servers>
    <server>
      <id>github</id>
      <username>attunedlabs</username>
      <password>***************</password>
    </server>
  </servers>
</settings>

```
