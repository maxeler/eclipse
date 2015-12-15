# Examples
* Operator overloading

#### Prerequisites
*Check main [README.md](https://github.com/maxeler/eclipse)*

## Import to Eclipse
Import the examples as a Java Project. The examples have test classes which demonstrate the the plug-ins' features.

## Using Maven
Compiling and running the examples using maven requires a modified [plexus-compiler-eclipse](https://github.com/maxeler/plexus-compiler/tree/eclipse-3.10.2-SNAPSHOT). Since this plug-in is not available on any public maven repository, it needs to be compiled and installed on the local repository.

### Checkout
Clone the repository and checkout the branch corresponding to your Eclipse version (e.g. Luna SR2):
```
git clone https://github.com/maxeler/eclipse.git
git checkout R4_4_maintenance
cd eclipse
```

### Build and install the plug-ins
This will install [plexus-compiler-eclipse](https://github.com/maxeler/plexus-compiler/tree/eclipse-3.10.2-SNAPSHOT) dependencies in the local maven repository:
```
mvn -P build-individual-bundles install -f eclipse-platform-parent/pom.xml
mvn -P build-individual-bundles install -f eclipse.jdt.core/pom.xml
mvn -P build-individual-bundles install -f eclipse.jdt.core/org.eclipse.jdt.core/pom.xml
```

### Checkout Maxeler's Plexus Compiler
Clone the repository and checkout the branch corresponding to the Eclipse Compiler version (e.g. Luna SR2):
```
git clone https://github.com/maxeler/plexus-compiler
git checkout eclipse-3.10.2-SNAPSHOT
cd plexus-compiler
```

### Build and install the plexus-compiler
This will install the [plexus-compiler](https://github.com/maxeler/plexus-compiler/tree/eclipse-3.10.2-SNAPSHOT) in the local maven repository:
```
mvn install -f plexus-compiler/pom.xml
mvn install -f plexus-compiler/plexus-compiler-api/pom.xml
mvn install -f plexus-compiler/plexus-compilers/pom.xml
mvn install -f plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml
```

### Run the example tests
On eclipse's checkout folder, run the following command:
```
mvn -f examples/pom.xml test
```
