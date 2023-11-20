# Project "connectors"

This module manages remote connections such as FTP, SFTP or AWS

## Create Remote connections in Docker
* Open cmd prompt and go to the path: batch-ingestion\docker-connectors
* In order to deploy FTP and SFTP docker images run the command:
> docker-compose up -d --build

## Generate SFTP private key
* Open cmf prompt in docker image and type:
  * ssh-keygen -t rsa -b 2048 -f sftpconnection
* Run the tool PUTTYGEN.EXE 
  * Load the private key --> Conversions --> Export Open SSH key (*.pem)

## Adding the dependency to another project
Adding the following code snipped into the new project's pom file as new dependency
>        <dependency>
>            <groupId>org.cereixeira</groupId>
>            <artifactId>connectors</artifactId>
>            <version>1.0-SNAPSHOT</version>
>        </dependency>

Importing the database module config class into the new project's config file
>       @Import({/*XYZ.class ,*/ ConnnectorsConfig.class})
>       public class CustomClassConfig {...}

Adding the config files to the new project's resource folder
>       For execution: config/connectors-application.properties
>       For testing: arc/test/resources/connectors-application.properties

## S3
Image: docker pull findify/s3mock
Crete container from image:
![](.\docker-aws.png)