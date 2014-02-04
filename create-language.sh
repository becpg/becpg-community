#!/bin/bash

for file in `find -name *_fr.properties`
do
	echo "creating  ${file%%_fr.*}_en.properties"
	cp ${file%%_fr.*}.properties ${file%%_fr.*}_en.properties
done
