# beCPG Developers README

## Build project

Ensure you are using Apache Maven version 3 or newer to build the project. If you don't have Maven installed, you can find it at https://maven.apache.org/.

To build the projects, use the following command:

```shell
$> mvn clean package -DskipTests=true 
```

The resulting AMP files can be located in the 'target' directories.

## Starting beCPG in Docker

Before you begin, make sure to install the necessary requirements:

Copy the sample docker-compose.override.yml.sample file to docker-compose.override.yml.

Execute the following commands:

```shell
$> ./run.sh build_start
```

For further guidance, refer to Alfresco Maven SDK 4.0 documentation.

## Running Tests

To execute all integration tests, use:

```shell
$> ./run.sh test
```

If you wish to run a specific test, employ the following command:

```shell
$> mvn test -Dtest=MyTest.java -DfailIfNoTests=false
```

Keep in mind that integration tests require the beCPG server to be launched first using the ./run.sh build_start command.

## Manual AMP Deployment

### Install becpg-designer

Install core AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-core/target/becpg-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-designer/becpg-designer-core/target/becpg-designer-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
```

Install share AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-share/target/becpg-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-designer/becpg-designer-share/target/becpg-designer-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
```

### Install becpg-project

Install core AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-core/target/becpg-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-project/becpg-project-core/target/becpg-project-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
```

Install share AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-share/target/becpg-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-project/becpg-project-share/target/becpg-project-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
```

### Install becpg-plm


Install core AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-core/target/becpg-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-designer/becpg-designer-core/target/becpg-designer-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-project/becpg-project-core/target/becpg-project-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-plm/becpg-plm-core/target/becpg-plm-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
```

Install share AMPS

```shell
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-share/target/becpg-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-designer/becpg-designer-share/target/becpg-designer-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-project/becpg-project-share/target/becpg-project-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
$>java -jar  $ALF/bin/alfresco-mmt.jar install ./becpg-plm/becpg-plm-share/target/becpg-plm-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
```

