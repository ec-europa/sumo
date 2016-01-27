#java -jar sumo.jar

install_folder=$(pwd)
#LD_LIBRARY_PATH=$install_folder/lib/ 
#GDAL_DATA=$install_folder/lib/
GDAL_DRIVER_PATH=$install_folder/lib/
java -Xdebug  -Dlog4j.configurationFile=./log4j2.xml -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:NewRatio=2 -Xmx3072m -cp ./lib/gdal.jar -Djava.library.path=$install_folder"/lib/" -Dncsa.hdf.hdf5lib.H5.hdf5lib=$install_folder"/lib/jhdf5so-2.11.0.so" -jar ./sumo.jar

