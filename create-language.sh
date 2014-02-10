#!/bin/bash

for file in `find -name *_fr.properties`
do
	echo "creating  ${file%%_fr.*}_en.properties"
	cp ${file%%_fr.*}.properties ${file%%_fr.*}_en.properties
done



for file in `find -name *_*.properties`
do
	cp $file /target/${file%%_fr.properties}
done