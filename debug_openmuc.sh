#!/bin/bash
cd framework
# Chạy trực tiếp Java với debug agent, bỏ qua script openmuc
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar felix/felix.jar
