#!/bin/bash

# Define variables
OPENMUC_VERSION="0.20.1"
DOWNLOAD_URL="https://oc.ise.fraunhofer.de/s/84kQoBcigyZf3PE/download"
TEMP_DIR="temp_openmuc_setup"

# Create temp directory
mkdir -p $TEMP_DIR
cd $TEMP_DIR

# Download OpenMUC
echo "Downloading OpenMUC ${OPENMUC_VERSION}..."
curl -L -o openmuc.tgz $DOWNLOAD_URL



# Extract
echo "Extracting..."
tar -xzf openmuc.tgz

# Copy directories
echo "Restoring framework files..."
cp -r openmuc/framework/bin ../framework/
cp -r openmuc/framework/felix ../framework/

# Cleanup
cd ..
rm -rf $TEMP_DIR

# Make executable
chmod +x framework/bin/openmuc

echo "OpenMUC framework setup complete!"
