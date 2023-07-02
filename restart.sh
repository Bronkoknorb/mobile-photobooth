#!/usr/bin/env bash

sudo docker build --pull --tag photoupp .

sudo docker stop photoupp
sudo docker rm photoupp
sudo docker run -d --restart always --net web-net --name photoupp --volume /home/ubuntu/photo-upp-data:/photo-upp-data photoupp
