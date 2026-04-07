#!/bin/bash

# This script rsync's the images from the rendering iMac, then copies them with new indexed names ffmpeg likes.
# Renamer skips any existing file, so you could delete both dirs and they'll be recreated.


clear
#set -x
sourceDir="../images6"
targetDir="../imagesNamed"
mpegName="5.0_to_15.0_1500.mp4"
bufferSize="100M"
maxRate="20M"

rm -rf $targetDir

mkdir $sourceDir
mkdir $targetDir
echo "Running rsync to fetch fresh images from iMac"
rsync -v --ignore-existing /Volumes/douglas_bullard/dev/github/douglasBullard/tracks6/*.png $sourceDir/

echo "Copying & renaming images ./gradlew runRenamer --args='-sourceDir $sourceDir -targetDir $targetDir'"

./gradlew runRenamer --args="-sourceDir $sourceDir -targetDir $targetDir"
rm $mpegName
echo "Creating animation - ffmpeg -f image2 -s 1500x1500 -i $targetDir/images_%5d.png -vcodec libx264  -bufsize $bufferSize  -maxrate $maxRate -crf 25 -n -pix_fmt yuv420p  $mpegName"
ffmpeg -f image2 -s 1500x1500 -i $targetDir/images_%5d.png -vcodec libx264 -bufsize $bufferSize  -maxrate $maxRate -crf 25 -n -pix_fmt yuv420p  $mpegName
open $mpegName