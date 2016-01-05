mvn install:install-file -Dfile=jarhdf5-2.11.0.jar -DgroupId=org.jrc.sumo -DartifactId=jhdf5 -Dversion=2.11.0 -Dpackaging=jar
mvn install:install-file -Dfile=jhdf5.dll -DgroupId=org.jrc.sumo -DartifactId=jhdf5dll -Dversion=2.11.0 -Dpackaging=dll
mvn install:install-file -Dfile=jarh5obj.jar -DgroupId=org.jrc.sumo -DartifactId=jhdf5obj -Dversion=2.11.0 -Dpackaging=jar
mvn install:install-file -Dfile=jarhdfobj.jar -DgroupId=org.jrc.sumo -DartifactId=jhdfobj -Dversion=2.11.0 -Dpackaging=jar