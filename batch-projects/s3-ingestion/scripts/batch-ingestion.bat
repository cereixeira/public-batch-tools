rem set JAVA_HOME=C:\desarrollo\Java\jdk-11.0.13
rem set PATH=%PATH%;%JAVA_HOME%\bin

java -jar s3-ingestion-1.0-SNAPSHOT.jar --spring.config.location=config/ --logging.config=config/logback.xml