# beCPG Developers README

## About beCPG PLM

beCPG is an open-source Product Lifecycle Management (PLM) software that oversees the entire lifecycle of a product, from conception and design to manufacture, service, and disposal. It facilitates collaboration on products and projects with customers and suppliers.

beCPG is tailored for Consumer Packaged Goods (CPG) industries, such as Food & Beverage and Cosmetics. What sets us apart is our comprehensive and user-friendly software, offered at a competitive price.

In summary, beCPG offers the following features:
- Product repository to manage finished products, recipes, raw materials, and packaging with technical and regulatory data.
- Formulation for automatic calculation of allergens, ingredients, nutrient facts, costs, labeling, etc.
- Product specification generator for clients, R&D, and production.
- Project management for handling new product development from ideas to market launch.
- Customer complaints management.

## beCPG Community

The beCPG community provides the main modules of beCPG as open source under the LGPLv3 license. This allows users to freely utilize the software.

Software documentation is available at [docs.becpg.fr](https://docs.becpg.fr).

The community version is a stable snapshot of the community trunk, occasionally updated with new features. Release dates for community versions are not fixed. The enterprise version includes specific features and packaging, with stable and qualified branches.

beCPG is composed of three Alfresco modules:

### beCPG Designer

beCPG Designer is a model designer module for Alfresco Share. It allows the creation, testing, and publishing of models and forms directly from Alfresco Share without manual XML file editing. It supports hot deployments and enables model and form creation for workflows. 

### beCPG PLM

beCPG PLM (Product Lifecycle Management) manages the entire product lifecycle, providing benefits such as improved time-to-market, decreased new product introduction costs, and adherence to standards and regulatory compliance. Key capabilities include product portfolio management, formula and recipe management, report generation (e.g., product specifications, user manuals), and new product development and introduction (NPD, NPI) with beCPG Project Manager.

### beCPG Project Manager

beCPG Project Manager is a project management module for Alfresco Share. It displays projects and tasks completion, expected deliverables, and offers Gantt views for project schedule and forecast.

## Build Project

Ensure you are using Apache Maven version 3 or newer to build the project. If Maven is not installed, you can find it at [https://maven.apache.org/](https://maven.apache.org/).

To build the projects, use the following command:

```shell
$ mvn clean package -DskipTests=true 
```

The resulting AMP files can be located in the 'target' directories.

## Starting beCPG in Docker

Before you begin, make sure to install the necessary requirements:

1. Copy the sample `docker-compose.override.yml.sample` file to `docker-compose.override.yml`.
2. Execute the following commands:

```shell
$ ./run.sh build_start
```

For further guidance, refer to Alfresco Maven SDK 4.0 documentation.

## Running Tests

To execute all integration tests, use:

```shell
$ ./run.sh test
```

If you wish to run a specific test, employ the following command:

```shell
$ mvn test -Dtest=MyTest.java -DfailIfNoTests=false
```

Keep in mind that integration tests require the beCPG server to be launched first using the `./run.sh build_start` command.

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

