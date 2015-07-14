# Xcode Maven Plugin

## Overview

The Xcode Maven Plugin can be used to integrate Xcode projects into a Maven build system. Especially it offers the following features:

* Integration with a Maven repository for binary artifact distribution
* Dependency resolution via a Maven repository between Xcode projects

## Known Limitations

* The xcode-maven-plugin is currently only compatible with maven 3.0. For more details check https://github.com/sap-production/xcode-maven-plugin/issues/131
* The xcode-maven-plugin is currently only compatible with Jdk 1.7.X, Project will build successfully with Jdk 8 but integration tests are failing, We recommend to use Jdk 1.7.x.

## Documentation

For a detailed documentation please refer to the Maven generated [Plugin documentation](http://sap-production.github.com/xcode-maven-plugin/site/index.html)

## License ##

This project is copyrighted by [SAP AG](http://www.sap.com/) and made available under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Please also confer to the text files "LICENSE" and "NOTICE" included with the project sources.


## Xcode Integration
To avoid command line usage and calling `mvn` manually, check out the initial version of this [Xcode plugin](https://github.com/sap-production/xcode-ide-maven-integration).
