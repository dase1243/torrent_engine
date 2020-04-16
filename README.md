TorrentEngine is a self-deploying project which allows you to set up a torrent 
on any type of VDS/cloud machine. In this README Gcloud is described as primary 
cloud service to run it.
This project is based on the next github repository:
atomashpolskiy/bt (https://github.com/atomashpolskiy)

How it works:
1) It is necessary to create an account on Gcloud from Google. In the bottom it
is described in details.

2) After that you should compile a jar file and then upload it into Cloud Storage
with the next commands:

mvn -DskipTests=true clean package
gsutil rm gs://{name of your storage}/torrent-engine-0.0.1-SNAPSHOT.jar 
gsutil cp target/torrent-engine-0.0.1-SNAPSHOT.jar gs://{name of your storage}/torrent-engine-0.0.1-SNAPSHOT.jar 

{name of your storage} - name of your storage where you upload you packages.

These commands are necessary to build and upload compiled jar into Cloud Storage 
with Gsutil python application

3) The file instance-startup.sh defines what will be run before jar file will be
deployed on the instance. In current version it requires only jdk installation,
compiled jar download and required dirs creation. After everything is set up, the
jar is executed.

4) After the server is deploed, you collect the ip of your created instance 
(how to create an instance is also described in google_cloud_startup.sh in 
commet area in the bottom. Copy of that will be at the bottom of README also) 
and connect to {ip}:8888/ with you browser.

5) Use it, as the interface is intuitive.


Addition:

How to create a gcloud account and get 300$ credit.
You just go to console: https://console.cloud.google.com/

And follow the instructions

How to create an instance from the console:
I attach the default server description which I use. More deep information 
about keys and values you can get on the next page:
https://cloud.google.com/sdk/gcloud/reference/compute/instances/create
Remember that some features or abilities of Gcloud, as for example GPU, 
requires more money consumption than simplier configurations.

My default settings for an instance:
gcloud compute instances create torrent-engine-instance-web \
 --image-family debian-9 \
 --image-project debian-cloud \
 --machine-type n1-highmem-8 \
 --scopes "userinfo-email,cloud-platform" \
 --metadata-from-file startup-script=instance-startup.sh \
 --metadata BUCKET=torrent_engine_web \
 --zone europe-west1-b \
 --tags http-server \
 --boot-disk-size 500GB


Remark:
In working-master branch non UI version is represented.

