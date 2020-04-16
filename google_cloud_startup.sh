#!/bin/sh

set username {Pass any username}
set pass {Here insert the password}

# Set the metadata server to the get projct id
cd ~/torrent-engine-project
git pull

expect -re "Username for 'https://gitlab.com':"
send "${username}\r"
expect -re "Password for 'https://dreikaa@gitlab.com':"
send "${password}\r"

mvn -DskipTests=true clean package
gsutil rm gs://torrent_engine_web/torrent-engine-0.0.1-SNAPSHOT.jar 
gsutil cp target/torrent-engine-0.0.1-SNAPSHOT.jar gs://torrent_engine_web/torrent-engine-0.0.1-SNAPSHOT.jar 

# gcloud compute instances create torrent-engine-instance-web \
# --image-family debian-9 \
# --image-project debian-cloud \
# --machine-type n1-highmem-8 \
# --scopes "userinfo-email,cloud-platform" \
# --metadata-from-file startup-script=instance-startup.sh \
# --metadata BUCKET=torrent_engine_web \
# --zone europe-west1-b \
# --tags http-server \
# --boot-disk-size 500GB