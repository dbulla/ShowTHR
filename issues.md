##### **ShoTHR Issues list**

- [ ] Skipping across the sand (i.e., dots)
  - [ ] Rho skips
  - [ ] Theta skips

- [ ] All of the track isn't being drawn - skip?
- [ ] Brighter sand
- [ ] Make the image sized "fixed" - or at least keep it's aspect ratio when scaled up
- [x] Rename options and document nicely
- [ ] Batch tracks
  - [ ] Fix it so it doesn't have to be the last option
  
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
  
- [ ] Make animation option
  - [ ] Takes the images that are drawn and saves them to a dir after displaying
  - [ ] Optional - wrap FFMPEG and create the animation 
    - [ ] Windows will suck

- [ ] Show duration from previous status output



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
        - [ ] Implies main server can listen to workers
        - [ ] Don't fail if server isn't available - wait XXX and try again forever







