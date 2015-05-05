# gl2gh_im - GitLab to GitHub issue migration

This is a project for migrating issues from GitLab to GitHub.

[![Build Status](https://travis-ci.org/rleh/gl2gh_im.svg?branch=master)](https://travis-ci.org/rleh/gl2gh_im)


## Build
Building is done using the included Gradle wrapper.
Dependencies are resolved automatically and included in jar archive.
```
./gradlew clean build
```
or on Windows:

```
gradlew.bat clean build
```

## Usage
```
java -jar build/libs/gl2gh_im.jar
```
The tool interactively asks for the required information.

