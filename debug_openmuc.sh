#!/bin/bash

echo "Updating bundles to ensure all dependencies are deployed..."
./gradlew updateBundles -x test

if [ $? -ne 0 ]; then
    echo "Failed to update bundles. Please check the error above."
    exit 1
fi

echo "Starting OpenMUC in debug mode..."
cd framework
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar felix/felix.jar
