metastore
----------
This project contains a flexible metadata, data and configuration information store. 
Anyone can use it but it was designed for use within the Pentaho software stack.

The "meta-model" is simple and very generic.
The top level entry is always a namespace. The namespace can be used by non-Pentaho companies to store their own information separate from anyone else.

The next level in the meta-model is an Element Type.  A very generic name was chosen on purpose to reflect the fact that you can store just about anything.  The element is at this point in time nothing more than a simple placeholder: an ID, a name and a description.

Finally, each element type can have a series of Elements.  
Each element has an ID and a set of key/value pairs (called "id" and "value") as child attributes. All attributes can have children of their own.
An element has security information: an owner and a set of owner-permissions describing who has which permission to use the element. (CRUD permissions)

The hierarchy:
--------------

* Namespace
* Element Type
* Element
* Attributes


#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

__Build for nightly/release__

All required profiles are activated by the presence of a property named "release".

```
$ mvn clean install -Drelease
```

This will build, unit test, and package the whole project (all of the sub-modules). The artifact will be generated in: ```target```

__Build for CI/dev__

The `release` builds will compile the source for production (meaning potential obfuscation and/or uglification). To build without that happening, just eliminate the `release` property.

```
$ mvn clean install
```

#### Running the tests

__Unit tests__

This will run all tests in the project (and sub-modules).
```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):
```
$ cd core
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```

__Integration tests__
In addition to the unit tests, there are integration tests in the core project.
```
$ mvn verify -DrunITs
```

To run a single integration test:
```
$ mvn verify -DrunITs -Dit.test=<<YourIT>>
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:
```
$ mvn verify -DrunITs -Dit.test=<<YourIT>> -Dmaven.failsafe.debug
```

__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory
````
