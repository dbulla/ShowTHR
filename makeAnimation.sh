#!/bin/bash

# This script rsync's the images from the rendering iMac, then copies them with new indexed names ffmpeg likes.
# Renamer skips any existing file, so you could delete both dirs and they'll be recreated.


clear
#set -x
sourceDir="images"
targetDir="imagesNamed"
mpegName="test_1500.mp4"

mkdir $sourceDir
mkdir $targetDir
echo "Running rsync to fetch fresh images from iMac"
rsync -v --ignore-existing /Volumes/douglas_bullard/dev/github/douglasBullard/sisyphus-table-pattern-maker/tracks4/*.png $sourceDir/
echo "Copying & renaming images"
./gradlew runRenamer --args="-sourceDir $sourceDir -targetDir $targetDir"
rm $mpegName
echo "Creating animation"
ffmpeg -f image2 -s 1500x1500 -i $targetDir/images_%5d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p  $mpegName
open $mpegName