## .MAXJ Editor
Supports **MaxJ syntax** on **.maxj** files:
  * Syntax highlighting
  * Code proposal, code completion, code templates
  * Refactoring
  * Outline
  * Search features

#### Prerequisites
*Check main [README.md](https://github.com/maxeler/eclipse)*

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
Copy the built plug-in .jar file to your Eclipse plug-ins folder, replacing the original plug-in (e.g. Luna SR2):
```
cp target/org.eclipse.jdt.ui-3.10.2-SNAPSHOT.jar <path-to-eclipse-luna-SR2>/plugins/org.eclipse.jdt.ui_3.10.2.v20141014-1419.jar
```
### Run
Launch Eclipse and check the features on **.maxj** files.
