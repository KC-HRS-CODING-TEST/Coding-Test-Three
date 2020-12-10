# Health Recovery Solutions Java Test

## Overview

- Write a small application that will read in rules.json file and then read in potentially different file formats 
containing sample patient readings, blood pressure, glucose, and weight.
    - Readings provided in json and yml formats but assume that other formats could be provided in the future.

- The application should compare all the readings to the rules and keep track of which readings trigger which rules 
(id.type.subtype and value should be reported in the end).

- After analyzing the results the application should send the findings off to another microservice within the stack via 
HTTP request with a json payload.
    - _The actual sending of the http request should not actually fire as it will have nowhere to go, so have the 
    application output `"Data Sync Success"` followed by the http request payload instead of actually firing off the request.
    Don't worry about the url or server's response, assume it will either respond with a 204 or 500 status code._
    - It is still expected to code everything except the actual sending of the request. 

- Additionally write findings as json file to a /code/results.json file (within the Docker container/runtime env itself). 

## Acceptance Criteria

The application:
- can be ran via `java -jar /path/to/file.jar readings.json` or `java -jar /path/to/file.jar readings.yml` (see below for Docker based instructions)
- *accurately* assesses which readings trigger which rules
- logs/outputs the http request payload of the data sync
- produces a json file

## System Requirements

- [Docker](https://www.docker.com/)

## Build and Execute

- From the command line, enter into this directory.

- Run the following command to build your Docker container which in turn will attempt to compile your code.
   
    ```shell script
    docker build -t hrs-test .
    ```

- Run the following command to execute your container (_this will run the jar file one time then disappear_).
    ```shell script
    docker run --rm hrs-test
    ```
    - Alternatively you can run the following in order to "enter" the container and manually run the java file and see the outputted json file.
    ```shell script
    docker run --rm -it --entrypoint /bin/bash hrs-test
    ```
    - You can optionally add any of the following args to pass values to the java runtime
        ```shell script
        -e READINGS_FILE=someFileName.extension
        -e RULES_FILE=someFileName.extension
        ```
        ie 
            ```
            docker run --rm -e READINGS_FILE=readings.yml hrs-test
            ```# Coding-Test-One
# Coding-Test-Three
