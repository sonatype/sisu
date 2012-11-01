
# to build example
mvn clean install

# to launch classic-classpath example...
java -jar guice-swing-main/target/main.jar

# to launch OSGi example with the old Felix text console...
java -jar guice-swing-main/target/main.jar osgi

# stop/start widget bundles to see them appear/disappear in the GUI

# PS: the 'nested' tab is supposed to nest the same set of tabs again,
# if you only have the nested tab the recursive effect may look odd :)

