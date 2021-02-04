<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.github.henneberger</groupId>
    <artifactId>typekin-tests</artifactId>
    <version>1.0.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.github.henneberger</groupId>
            <artifactId>typekin-annotation</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.github.henneberger</groupId>
            <artifactId>typekin-processor</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>
      <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.16</version>
        <scope>provided</scope>
      </dependency>
    </dependencies>
    
</project>