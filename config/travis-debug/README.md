#Travis CI Debug Environment for Mache

You can build this dockerfile to create a docker image that can then be used for debugging the Mache Travis CI builds.

##Building

```
docker build -t excelian/mache-travis-debug .
```

##Running the Build

```
docker run -it excelian/mache-travis-debug --name mache-travis-debug

./build.sh
```

##Changing Branches

By default, the docker container will be set up to run the master build. If you need to change this, perform the following:

```
cd ~/build
./change-branch.sh your-branch-name
./build.sh
```
##Rerunning Builds

The current build scripts assume we're starting from a fresh container, but always starting from a fresh container can be slow going as we download all of the dependencies again. An alternative is to just keep your container and start/attach to it. For example, after you have run a previous build and have exited, reattach and rerun with the following:

```
docker start mache-travis-debug
docker attach mache-travis-debug
./build.sh
```
