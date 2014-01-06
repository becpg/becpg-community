#!/bin/sh

for i in `ls *.csv`
do
 echo "iconv -t utf-8 -f iso-8859-15 $i > $i.new"
 #iconv -t utf-8 -f iso-8859-15 $i > $i.new
 ./utf2iso.py $i
done

