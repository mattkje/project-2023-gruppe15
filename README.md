
# Greenhouse application - Group 15

# this-repo-is-deprecated-since-05102020

[New repo](https://github.com/ntnu-datakomm/course-project-team-15).

_Contributing members: Matti Kjellstadli, Håkon Svensen Karlsen, Di Xie, Adrian Faustino Johansen_

## About this project

Course project for the
course [IDATA2304 Computer communication and network programming (2023)](https://www.ntnu.edu/studies/courses/IDATA2304/2023).

Project theme: a distributed smart greenhouse application, consisting of:

* Sensor-actuator nodes
* Visualization nodes

See protocol description in [protocol.md](protocol.md).

## Getting started

There are several runnable classes in the project.

To run the greenhouse part (with sensor/actuator nodes):

* Command line version: run the `main` method inside `CommandLineGreenhouse` class.
* GUI version: run the `main` method inside `GreenhouseGuiStarter` class. Note - if you run the
  `GreenhouseApplication` class directly, JavaFX will complain that it can't find necessary modules.

To run the control panel (only GUI-version is available): run the `main` method inside the
`ControlPanelStarter` class

## Simulating events

If you want to simulate fake communication (just some periodic events happening), you can run
both the greenhouse and control panel parts with a command line parameter `fake`. Check out
classes in the [`no.gruppe15.run` package](src/main/java/no/gruppe15/run) for more details. 

