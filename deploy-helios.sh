#!/bin/zsh

PROJECT_PATH="$HOME/IdeaProjects/IS-course/"

SERVER_PATH="$PROJECT_PATH/server"
CLIENT_PATH="$PROJECT_PATH/client"

JAR_PATH="$SERVER_PATH/build/libs/server-1.0.jar"
FRONT_BUILD_PATH="$CLIENT_PATH/dist/client/browser"

cd "$SERVER_PATH"
gradle bootJar

cd "$CLIENT_PATH"
ng build --base-href="/~s367370/is/browser/"

scp "$JAR_PATH" helios:~/IS/course
scp -r "$FRONT_BUILD_PATH" helios:~/public_html/is