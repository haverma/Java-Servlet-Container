## Dockerfile to build and run an image for Batfishserver

Currently this dockerfile supports running Batfish image in these modes
   *Allinone Mode
   *Distributed Scalable Mode


### Building Docker Batfish image

1. Put allinone-0.28.1.jar, batfish-bundle-0.28.1.jar, coordinator-bundle-0.28.1.jar and batfish-client-bundle-0.28.1.jar in assets/bundled_jars directory and
   question-0.28.1.jar under assets/bundled_jars/question_plugin directory
2. Run `docker build . -t <Image-Name:Tag>` in the root folder, this will build the batfish docker image.




### Running the image in different modes
#### Allinone mode

##### Running the server
* Run `docker run <Image-Name:Tag> -m allinone` from the project root, this starts the Batfish server

##### Running Batfish Java client
* Open another terminal, run `docker network inspect bridge`
* From the output, find the node under "Containers" that corresponds to the "allinone" container for the Batfish server
* Copy the IPv4Address from that node (this is the IP Address that the Java Client can use to reach the Batfish server)
* To run Java Client in interactive mode use `docker run -v <host dir>:/root/workdir/<container dir> -it <Image-Name:Tag> -m client -h <Batfish-Servr IP>`, `Batfish-Server IP` is found in the   above step.
  `host dir` should be an absolute path and may contain the test rigs(and possibly other files) that are to be used in the Java client. 
  `container dir` is a directory mounted under `root/workdir` and is parallel to the path from where the Batfish client is run in the container. So if the volume is specified as `-v /home/user1/docker/test_rigs:/root/workdir/test_rigs`, a test rig can be accessed as `init-testrig test_rigs/example` in the Java client
* To run the Java Client in batch mode use `docker run -v <host dir>:/root/workdir/<container dir> -it <Image-Name:Tag> -m client -h <Batfish-Servr IP> -f <cmd-file>`, where path of `cmd-  file` will be relative to `/root/workdir` as in interactive usage





#### Distributed Scalable mode

1. Run `docker swarm init` to enable the swarm mode, check https://docs.docker.com/engine/swarm/ for additional configurations(not required for running Batfish).

##### Running only the Batfish server
   2. In `docker-compose.yml`, under `worker->deploy->replicas`, adjust the number of replicas, and in `resources` limit the cpu and memory allocated (if required)
   3. Run `docker stack deploy -c docker-compose.yml <app-name>`, `app-name` can be any preferred name for the app.
   4. This will spawn one copy of coordinator and multiple copies of workers as specified in `docker-compose.yml`.
   

##### Running Batfish server with Client
   2. Make the same changes as in step 2 above in `docker-compose-client.yml`
   3. Also under `client->volumes`, map a `host dir` to `container dir` as shown above. Note that here `host dir` can be relative to docker-compose-client.yml   file.
      ##### Running the client in interactive mode
         4. Run `docker stack deploy -c docker-compose-client.yml <App-Name>`, this will spawn the coordinator, worker and the client services.
         5. Now to attach to the running client container by following these steps
            * Run `docker ps` and find the container id of the client, (the name of the client container will be of the format *<App-Name>_client.1.<some random id>*)
            * Run `docker attach <client container id>` and you will get a blank prompt, press `Enter` and the Batfish client prompt should be shown now
            * The location of the mounted dir will be the same as explained above in the Allinone mode, use the location in running the commands
            * Exit the Java client by using `Ctrl+p` and `Ctrl+q`
      ##### Running the client in batch mode
         4. In `docker-compose-client.yml` append arguments in `client->command` for the batch file containing the commands using [ ... "-f","<path to command file>"], again path
           of the command file will be relative to /root/workdir as explained above.
         5. Run `docker stack deploy -c docker-compose-client.yml <App-Name>`, this will spawn the coordinator, worker and the client services and run the command file in the client.
         6. To see the status of the command run `docker logs <client-container id>`, where client-container id can be found as shown above

