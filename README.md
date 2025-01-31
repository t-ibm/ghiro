# Ghiro
A minimal viable product implementing a Transaction Oriented Middleware approach.

[![Build Status](https://us-builds.ame.ad.sag/buildStatus/icon?job=GHIRO_master)](https://us-builds.ame.ad.sag/job/GHIRO_master/)
[![Quality Gate](https://sonar.eur.ad.sag/api/project_badges/measure?project=com.softwareag.tom.ghiro:ghiro&metric=alert_status)](https://sonar.eur.ad.sag/dashboard?id=com.softwareag.tom.ghiro:ghiro)

#### Sub-Systems
* [Contract](./modules/contract)
* [Protocol](./modules/protocol)
* [IS Packages](./packages)

#### Architecture
A multi-layered [architecture](./architecture.md) for decentralized applications.

|Layer 4|Quality of Service|
|------:|:-----------------|
|**Layer 3**|**Decentralized Applications**|
|**Layer 2**|**Distributed Ledger Technology**|
|**Layer 1**|**Network Infrastructure**|

#### Platform
As the underlying platform we are using a Gentoo distribution containing the necessary development tools as well as a single
node integration test environment. It is available as a Docker image [sagtom/gentoo-dev](https://hub.docker.com/r/sagtom/gentoo-dev)
via Docker Hub.

After spawning-off the container login as a non-root user and configure your environment.
````
$ ssh -i ~/.ssh/id_ed25519 tglaeser@niue.eur.ad.sag
tglaeser@niue ~ $ echo export GOPATH=/usr/local/go >> ~/.bashrc
tglaeser@niue ~ $ echo export PATH=\$PATH:\$GOPATH/bin >> ~/.bashrc
tglaeser@niue ~ $ git config --global user.name "Thomas Glaeser"
tglaeser@niue ~ $ git config --global user.email thomas@webmethods.com
tglaeser@niue ~ $ git config --global core.editor nano
tglaeser@niue ~ $ mkdir projects
tglaeser@niue ~ $ cd ./projects
````

#### Prerequisites
This project requires a Java JDK version 8 or higher to be installed.
To check, run `javac -version`

#### Get the Sources
```
$ git clone git@github.softwareag.com:tglaeser/ghiro.git
$ cd ./ghiro
```

#### Displays the Project Structure
```
$ ./gradlew projects
```

#### Build Default Tasks
```
$ ./gradlew
```

#### Sync the Decentralized Application packages to an IS instance
Access to layer 3 is represented by IS packages `WmDApp` and `WmDAppContract`.
```
$ ./gradlew syncPkg -Dcom.softwareag.tom.test.is.instance.dir=${sagRootDir}/IntegrationServer/instances/default
```

#### Run Burrow
Layers 1 and 2 are provided by `burrow`.
```
$ ./gradlew spawnBurrow
```

#### More Build Information
```
$ ./gradlew help
```

#### More Information
Gradle projects are self descriptive. See the [Gradle documentation](https://gradle.org/docs) for more information.
