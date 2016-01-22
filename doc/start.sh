install_folder=$(pwd)
java -Dlog4j.configurationFile=./log4j2.xml -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:NewRatio=2 -Xmx3072m -Djava.library.path="./lib/" -Dncsa.hdf.hdf5lib.H5.hdf5lib=$install_folder"/lib/libjhdf5.so" -jar ./sumo.jar

#to add -Djava.library.path ="%CD%/lib/gdal_lib_win64/bin/gdal/java;%CD%/lib/gdal_lib_win64/bin/gdal/plugins/"
