# Maxeler Eclipse Plugins

## Eclipse Compiler for Java
Supports Maxeler Java syntax.

### Prerequisites
#### Oracle Java 1.6 or higher
* Java 1.6 JDK needs to be on PATH.
* Set JAVA_HOME to point to your JDK
* Ensure your java is set to run in Server mode

##### Java Server Mode
A parameter used by the build "-XX:-UseLoopPredicate" is not recognized by java when running in Client mode. To check which mode you are running in by default run:
```
$ java -version
java version "1.7.0_09"
Java(TM) SE Runtime Environment (build 1.7.0_09-b05)
Java HotSpot(TM) 64-Bit Server VM (build 23.5-b02, mixed mode)
```
If the last line says "Client VM" instead of "Server VM" then you are running in client mode. In which case you will need to modify the file **/jdk1.7.0_09/jre/lib/amd64/jvm.cfg** and ensure that the line **-server KNOWN** is the first line in the file.

#### Apache Maven 3.1.1 or higher
* Download from http://maven.apache.org/download.html
* make sure **mvn** is available in your PATH
* *_Notice:_* Apache 3.3.3 requires Java 7.


### Checkout
Clone the repository and checkout the branch corresponding to your Eclipse version (e.g. Luna SR2):
```
git clone https://github.com/maxeler/eclipse.git
git checkout R4_4_maintenance
```
### Build:
```
cd eclipse.jdt.core/org.eclipse.jdt.core
mvn -P build-individual-bundles package
```
### Install
Copy the built plugin .jar file to your Eclipse plugins folder, replacing the original plugin (e.g. Luna SR2):
```
cp target/org.eclipse.jdt.core-3.10.2-SNAPSHOT.jar <path-to-eclipse-luna-SR2>/plugins/org.eclipse.jdt.core_3.10.2.v20150120-1634.jar
```
### Run
Launch Eclipse and import your MAX project source code as a Java Project.
