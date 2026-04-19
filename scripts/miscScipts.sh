#!/bin/bash

rm ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4/*
cp ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/tracks4/*.png ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4/
cg run | more
clear;rm -f test_500_2.mp4 && ffmpeg -f image2 -s 500x500 -i ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4/image_%5d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p -r 60  test_500_2.mp4
clear;rm -f test_500_2.mp4 && ffmpeg -f image2 -s 500x500 -i ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4/images_%4d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p -r 60  test_500_2.mp4
ll ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/tracks4/*.png | wc -l
ll ~/dev/github/douglasBullard/sisyphus-table-pattern-maker/tracks4/*.thr | wc -l