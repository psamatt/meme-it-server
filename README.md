# Meme It - Backend

## Introduction
Meme-It is the ultimate meme creation application, guaranteed to bring high entertainment value to a night with friends and family, or make waiting in the dentist office alone just a bit more bearable. Meme it can be played with friends by joining the game with a specific code, or against strangers by joining one of the public lobbies.

The rules are simple: a group of players receive randomly chosen meme templates to add their own text to and create a unique meme through their sense of humor. After a time limit, the memes are collected and voted on, with the best meme winning the round. The collaborative chat feature allows users to chat with their opponents while waiting for the game to start, as well as during the game. 

## Technologies
- Java
- Spring Boot
- Spring Data JPA
- Mockito
- Gradle

## High-level Components
The frontend of Meme-It is the interface that users interact with to create and customize their memes. It also
utilizes API calls to the backend to display meme data. The backend of Meme-It is responsible for processing user requests and managing the application’s data. It includes a server that handles incoming requests, a database to store meme data, and an restful API that communicates with the frontend.


## Launch and Deployment Guide
This guide provides instructions on how to launch and deploy the project using IntelliJ and VS Code. Please follow the steps below:

### IntelliJ
1. Open IntelliJ and navigate to File -> Open.... Select the Meme It project.
2. When prompted, choose to import the project as a `gradle project`.
3. To build the project, right-click on the `build.gradle` file and select `Run Build`.

### VS Code
To facilitate the setup process in VS Code, consider installing the following extensions:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

**Note:** Before proceeding, ensure that you have built the project with Gradle. You can do this by clicking on the `build` command in the  _Gradle Tasks_ extension. Once the project is built, check the _Spring Boot Dashboard_ extension. If it already displays the project, you can click the play button to start the server. If the project does not appear, restart VS Code and check again.

## Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test

```bash
./gradlew test
```

## Roadmap
- convert current tutorial to make it an interactive walkthrough tutorial
- provide option for messaging users of the current game privately

## Authors and Acknowledgement
Made by SoPraFS23 Group 13.

- Daniel Lutziger
- Pablo Tanner
- Linda Weber
- Henrik Nordgren
- Marc Schurr

## License
MIT License.
The license is provisional and subject to change. All rights reserved.