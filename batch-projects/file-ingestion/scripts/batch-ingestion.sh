#!/bin/sh

# export JAVA_HOME=/opt/openjdk-11.0.12
# export PATH=$JAVA_HOME/bin:$PATH

java -jar file-ingestion-1.0-SNAPSHOT.jar --spring.config.location=config/ --logging.config=config/logback.xml