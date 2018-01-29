# Xcode Maven Plugin

## Finally..!! This repo can atlast be retired..!!
The way xcode-maven-plugin building a project (generation of .app and archive to ipa) is no more supported by apple. Generated ipa is not  eligible for appStore upload. Changes around the xcode are very dynamic it's not realistic to adopt these changes to xcode-maven-plugin. We will not assure further contribution to this project from SAP. Officially closing our support to this repository.

Please refer for Apple's latest [App Distribution Workflow](https://developer.apple.com/library/content/documentation/IDEs/Conceptual/AppDistributionGuide/Introduction/Introduction.html#//apple_ref/doc/uid/TP40012582-CH1-SW1)

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
