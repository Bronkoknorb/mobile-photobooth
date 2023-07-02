# PhotoUpp

Wedding photobooth that allows uploading images from your smartphone using a photo app.

Photos are shown live on a screen.

## Build

    docker build --pull --tag photoupp .

## Run

    docker run -p 8082:8080 --volume ~/photo-upp-data:/photo-upp-data photoupp

## My Build & Run script

    ./restart.sh
