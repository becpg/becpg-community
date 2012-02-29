#!/usr/bin/perl
use File::Basename;

 

$birt_version="3.7.2";
$groupId="org.eclipse.birt";
open(OUSH, ">mavenize.sh");
open(OUDEP, ">pombirt.xml");

 

@files = <*.jar>;
 foreach $file (@files) {
   $bf = fileparse($file, ".jar");
   print OUSH "mvn install:install-file -DgroupId=$groupId -DartifactId=$bf -Dversion=$birt_version -Dpackaging=jar -Dfile=$file\n";
   print OUDEP <<OKI
   <dependency>
       <groupId>$groupId</groupId>
       <artifactId>$bf</artifactId>
       <version>$birt_version</version>
   </dependency>
OKI
 }

