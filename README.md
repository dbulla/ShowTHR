# ShowTHR

Read a THR file and simulate the motion of a ball rolling over a table covered in sand.

This is a fork of [ShowTHR](https://github.com/MarginallyClever/ShowTHR) with some major changes:
- Migrated build system from Maven to Gradle (hey, I like Gradle :) )
- Migrated from Java to Kotlin (I _really_ like Kotlin)
- 2-ball simulations
- Reverse-order track
- Use a background image for the initial sand depth (i.e., like having done a "clean" before the track)
- Real-time simulation — like watching the table itself draw
- Make an .mp4 of the track's movement
- Draw multiple, consecutive tracks (i.e., each track overwrites the previous) - and again, can make an .mp4 of this

### **Typical usage:**

- Build and run from Gradle: 
  - `./gradlew run  --args="-i clockworkSwirl5WithClipping.thr`
  - if `-tableSize` is not specified, the screen size will be used

- Run from Java:
  - First, build the jar: 
    - `./gradlew shadowJar`

  - Then run the jar: 
    - `java -jar build/libs/ShowTHR-all.jar -i clockworkSwirl5WithClipping.thr`

- Make an animation (output 1000x1000 pixels, sped up): 
  - `./gradlew run --args="-makeAnimation -tableSize 1000 -i offset_spiral_2.thr -skip 1600`

- Make an animation of multiple tracks playing (outout 1000x1000 pixels): 
  - `./gradlew run --args="-makeAnimation -tableSize 1000 -batchTracks offset_spiral_2,clean,dougsSpiralTriangle2,clean,Broken_Wings,clean -skip 1600`

# **ShowTHR Options**

### Mandatory arguments requiring values:

| Option | Description    | Meaning                                  |
|--------|----------------|------------------------------------------|
| -i     | Input filename | .thr track name - be careful with spaces |



### Optional arguments requiring values:

| Option          | Description           | Meaning                                                                                                 |
|-----------------|:----------------------|:--------------------------------------------------------------------------------------------------------|
| -background     | Background image name | use the image as the starting background                                                                |
| -ballRadius     | Ball size             | Radius of the ball in pixels                                                                            |
| -batchTrackFile | Batch tracks file     | Read the tracks from a batch file like 'tracks.txt'.  These files can have attributes like 2Balls, etc. |
| -batchTracks    | Batch tracks list     | A comma separated list of .thr files to process.  Overridden by -batchTrackFile                         |
| -depth          | Sand depth            | Initial depth of the sand in pixels (only used if no background image is supplied)                      |
| -o              | Output file name      | Optional - if not supplied, the inputFilename will be used with a .png extension                        |
| -skip           | Skip count            | Only render images to GUI (or disk) every `skip` renderings                                             |
| -tableSize      | Table size (diameter) | Resultant image will be tableSize x tableSize pixels.   If not supplied, the screen size will be used   |

### Optional no-value arguments:

| Option             | Description                    | Meaning                                                                                                                                                                                        |
|--------------------|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -2balls            | Two balls are better than one! | If your table has the ability to use 2 balls, this simulates that                                                                                                                              |
| -doClean           |                                | If present, clean the sand before rendering.                                                                                                                                                   |
| -expandTracks      |                                | Expand sequences of points to improve rendering quality                                                                                                                                        |
| -gray              |                                | Use a grey background instead of the image background                                                                                                                                          |
| -headless          |                                | No GUI, just render the image - this works on devices with no display.  Fastest way to generate the whole track.                                                                               |
| -makeAnimation     |                                | Make animation frames of the images - you can run ffmpeg to create a video.  Dir is 'animationImages'                                                                                          |
| -makeCleanBackdrop |                                | Make a backdrop image of clean sand for use in future renderings, will be saved with a name like `clean_800x800.png`.  Putting the screen size in the name lets it be used as a backdrop later |
| -q                 | Quit                           | Quit after rendering the image (i.e., shuts down the GUI)                                                                                                                                      |
| -reversed          | Reversed                       | Render the track in reverse order (backwards)                                                                                                                                                  |



Get the [Release](https://github.com/douglasbullard/ShowTHR/releases) version or build it yourself from source code.


## Example

```java -jar ShowTHR.jar "src/test/resources/Vaporeon with Waves.thr" sand_simulation.png -w 1000 -h 1000```

should produce the following:

![Example](testTracks/sand_simulation.png)

## Notes

The intensity of the output image is dictated by the highest peak in the sand simulation.  The output image is normalized to the range [0, 255].
If one point of sand is very tall, the rest of the image will be very dark.

## License

Apache 2.0 License
