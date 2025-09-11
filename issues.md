##### **ShoTHR Issues list**

- [ ] Skipping across the sand (i.e., dots)
  - [ ] Rho skips
  - [ ] Theta skips

- [ ] All of the track isn't being drawn - skip?
- [ ] Brighter sand
- [ ] Black background outside of sand (i.e., depth 0 for rho>1)
- [ ] Make the image sized "fixed" - or at least keep it's aspect ratio when scaled up
- [x] Rename options and document nicely
- [ ] Batch tracks
  - [x] Fix it so it doesn't have to be the last option
  
  - [ ]  Make a batch file version 
    - [ ] Not just a list of tracks, add commands 
      - [ ] At startup
        - [ ] Table size
        - [ ] Skip count
        - [ ] Initial background
  
      - [ ] For each track
        - [ ] Clean
        - [ ] 2 Balls
        - [ ] Reversed
        - [ ] Skip Count
  
- [x] Make animation option
  - [x] Takes the images that are drawn and saves them to a dir after displaying
  - [x] Optional - wrap FFMPEG and create the animation 
    - [x] Windows will suck
  - [ ] Beware of darkness - multiple tracks keep getting darker and darker

- [ ] Show duration from previous status output

- [ ] ~~Move to +/- x, y - only map to final output at rendering~~
  - [ ] ~~Instead of x from 0...2*radius, make it -radius to +radius~~
  - [ ] ~~Simplifies rho/theta conversion~~
  - [ ] ~~Perform offset only when rendering~~
  - [ ] Shit, array logic is all 0..2*radius!!!!!




##### **Long Term - Rendering farm**

- [ ] Make a rendering farm of iMac & Raspberry Pi's?
  - [ ] Need to coordinate which images go to which pi to avoid duplication.
    - [ ] Make a web service for each pi which listens for image generation commands
      - [ ] Draw Image
      - [ ] Status of drawn image
      - [ ] Fetch drawn image
      - [ ] Delete image
    - [ ] Main Mac will have a program that coordinates with the workers
      - [ ] Can they broadcast availability to server at startup?
        - [ ] Need to know server IP
        - [ ] Implies main server can listen to workers - or ping them for status
      - [ ] What happens when server is down?
        - [ ] Don't fail if server isn't available - wait XXX and try again forever
      - [ ] What happens if worker dies? i.e., unfinished assigned job







