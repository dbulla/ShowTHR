#!/bin/bash

clear
#set -x

mkdir outputTemp
rsync -a douglasbullard@192.168.1.46:/home/douglasbullard/pingStuff/ outputTemp
#rsync -a douglasbullard@192.168.1.217:/home/douglasbullard/pingStuff/ outputTemp
./gradlew runAnalyzeOutput --args="outputTemp"
