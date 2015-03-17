#!/bin/bash

CWD=`pwd`

cd ..

sbt ticksfeed/assembly

cp ticksfeed/target/scala-2.10/ticksfeed-assembly-0.1-SNAPSHOT.jar $CWD/target/