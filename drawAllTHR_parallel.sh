#!/bin/bash


# Specify the directory and file extension
#directory="SisyphusTracks"
directory="$1"
extension=".thr"

# Command to execute on each file
command="java -jar ./build/libs/ShowTHR-all.jar"

#set -x
MAX_CONCURRENT_JOBS=5

./gradlew clean shadowJar
# Loop through all files with the specified extension
for file in "$directory"/*"$extension"; do
    # Check if the file is a regular file
    if [ -f "$file" ]; then
      # Check if the .png already exists - if so, skip and proceed to the next file
        png_file="${file%.*}.png"
        if [ ! -f "$png_file" ]; then
            while true; do
                CURRENT_JOBS=$(jobs -p | wc -l)
                if [[ $CURRENT_JOBS -lt $MAX_CONCURRENT_JOBS ]]; then
                    break
                else
                    echo "Waiting for a job slot to open... ($CURRENT_JOBS running)"
                    sleep 5 # Wait for a short period before rechecking
                fi
            done
            commandText="$command -i $file -q -h 1500 -w 1500 -headless"
            echo "Starting: $commandText"
            bash -c "$commandText" &
        else
          echo "Skipping $file, as $png_file already exists"
        fi
    fi
done

# Wait for all background jobs to complete
echo "All commands submitted. Waiting for them to finish..."
wait

echo "All commands completed."
