#!/bin/bash


# Specify the directory and file extension
#directory="SisyphusTracks"
directory="$1"
extension=".thr"

# Command to execute on each file
command="java -jar ./build/libs/ShowTHR-0.0.1-SNAPSHOT-all.jar "

#set -x

./gradlew clean shadowJar
date
# Loop through all files with the specified extension
for file in "$directory"/*"$extension"; do
    # Check if the file is a regular file
    if [ -f "$file" ]; then
        # Execute the command on the file
        $command "$file"
        # display the image when it's done
        open "${file/.thr/.png}"
        echo
        echo
        echo
    fi
#    break
done
date