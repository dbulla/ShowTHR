#!/bin/bash

# This script rsync's the images from the rendering iMac, then copies them with new indexed names ffmpeg likes.
# Renamer skips any existing file, so you could delete both dirs and they'll be recreated.


clear
#set -xhisto
sourceDir="../images6"
targetDir="../imagesNamed"
mpegName="5.0_to_15.0_1500.mp4"

# Clean out previous files
# rm -rf $targetDir

mkdir -p $sourceDir
mkdir -p $targetDir
echo "Running rsync to fetch fresh images from iMac"
#rsync -v --ignore-existing /Volumes/douglas_bullard/dev/github/douglasBullard/sisyphus-table-pattern-maker/tracks4/*.png $sourceDir/
echo "rsync -v --ignore-existing /Volumes/douglas_bullard/dev/github/douglasBullard/tracks6/*.png $sourceDir/"
rsync -v --ignore-existing /Volumes/douglas_bullard/dev/github/douglasBullard/tracks6/*.png $sourceDir/
echo "Copying & renaming images"
./gradlew runRenamer --args="-sourceDir $sourceDir -targetDir $targetDir"
rm $mpegName
echo "Creating animation: ffmpeg -f image2 -s 1500x1500 -i $targetDir/images_%6d.png -vcodec libx264 -bufsize 100M -maxrate 20M -crf 25 -n -pix_fmt yuv420p $mpegName"
ffmpeg -f image2 -s 1500x1500 -i $targetDir/images_%6d.png -vcodec libx264 -bufsize 100M -maxrate 20M  -crf 25 -n -pix_fmt yuv420p $mpegName
open $mpegName