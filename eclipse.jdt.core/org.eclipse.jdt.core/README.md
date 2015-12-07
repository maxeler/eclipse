## Eclipse Compiler for Java
Supports **operator overloading**, which is part of Maxeler Java syntax.

#### Prerequisites
*Check main [README.md](https://github.com/maxeler/eclipse/tree/R4_4_maintenance)*

### Checkout
Clone the repository and checkout the branch corresponding to your Eclipse version (e.g. Luna SR2):
```
git clone https://github.com/maxeler/eclipse.git
git checkout R4_4_maintenance
```
### Build
```
cd eclipse/eclipse.jdt.core/org.eclipse.jdt.core
mvn -P build-individual-bundles package
```

### Install
Copy the built plug-in .jar file to your Eclipse plug-ins folder (e.g. Luna SR2):
```
cp target/org.eclipse.jdt.core-3.10.2-SNAPSHOT.jar <path-to-eclipse-luna-SR2>/plugins/
```

### Configure
Copy the "Bundle-Version" from the built MANIFEST.MF file:
```
$ grep -e "Bundle-Version: " eclipse.jdt.core/org.eclipse.jdt.core/target/MANIFEST.MF
Bundle-Version: 3.10.2.v20151116-1448
```
Edit the following configuration file, in your eclipse's configuration directory:
```
<path-to-eclipse-luna-SR2>/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
```
update the following line:
```
org.eclipse.jdt.core,3.10.2.v20150120-1634,plugins/org.eclipse.jdt.core_3.10.2.v20150120-1634.jar,4,false
```
with the correct "Bundle-Version" and file name:
```
org.eclipse.jdt.core,3.10.2.v20151116-1448,plugins/org.eclipse.jdt.core-3.10.2-SNAPSHOT.jar,4,false
```

### Run
Launch Eclipse and import your MAX project source code as a Java Project.
