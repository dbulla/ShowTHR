#!/bin/bash

# Builds the app, then draws all the .thr files in the given directory

# Specify the directory and file extension
directory="$1"
extension=".thr"

# Command to execute on each file
command="java -jar ./build/libs/ShowTHR-*-all.jar"

#set -x

./gradlew clean shadowJar
date
# Loop through all files with the specified extension
for file in "$directory"/*"$extension"; do
    # Check if the file is a regular file
    if [ -f "$file" ]; then
        # Execute the command on the file
        $command -i "$file" -tableDiameter 1000 -skip 20 -quit
        $command -i "$file" -tableDiameter 1000 -skip 20 -quit -tantalus

        # display the image when it's done
#        open "${file/.thr/.png}"
        echo
        echo
        echo
        sleep 0.2
    fi
#    break
done

date
