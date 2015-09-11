#!/bin/bash

#mvn clean

for file in `find -name *_fr.properties`
do
	echo "creating  ${file%%_fr.*}_en.properties"
	cp ${file%%_fr.*}.properties ${file%%_fr.*}_en.properties
done


for file in `find -name *_*.properties -not -path "*bin*"`
do
        mkdir -p target/properties/$(dirname ${file})
	cp $file target/properties/$(dirname ${file})/
        echo "Copying target/properties/$(dirname ${file})/$file"
done

