#!/bin/bash

clear
#set -x

#mkdir outputTemp
mkdir outputTempOld
#rsync -a douglasbullard@192.168.1.46:/home/douglasbullard/pingStuff/ outputTemp
rsync -a douglasbullard@192.168.1.217:/home/douglasbullard/pingStuff/ outputTempOld
./gradlew runAnalyzeOutput --args="outputTempOld"
