## .MAXJ Editor
Supports **MaxJ syntax** on **.maxj** files:
  * Syntax highlighting
  * Code proposal, code completion, code templates
  * Refactoring
  * Outline
  * Search features

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
cd eclipse/eclipse.jdt.ui/org.eclipse.jdt.ui
mvn -P build-individual-bundles package
```

### Install
Copy the built plug-in .jar file to your Eclipse plug-ins folder (e.g. Luna SR2):
```
cp target/org.eclipse.jdt.ui-3.10.2-SNAPSHOT.jar <path-to-eclipse-luna-SR2>/plugins/
```

### Configure
Edit the following configuration file, in your eclipse's configuration directory:
```
<path-to-eclipse-luna-SR2>/configuration/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
```
update the following line:
```
org.eclipse.jdt.ui,3.10.2.v20141014-1419,plugins/org.eclipse.jdt.ui_3.10.2.v20141014-1419.jar,4,false
```
with the version in the MANIFEST.MF file and the correct file name:
```
org.eclipse.jdt.ui,3.10.2.v20151116-1448,plugins/org.eclipse.jdt.ui-3.10.2-SNAPSHOT.jar,4,false
```

### Run
Launch Eclipse and check the features on **.maxj** files.
