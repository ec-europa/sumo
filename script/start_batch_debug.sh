#!/bin/sh
install_folder=$(pwd)
GDAL_DRIVER_PATH=$install_folder/lib/
java -Xdebug -Xrunjdwp:transport=dt_socket,address=8999,server=y -Dlog4j.configurationFile=./log4j2.xml -Dsun.java2d.noddraw=true -Djava.library.path=$install_folder"/lib/" -Dncsa.hdf.hdf5lib.H5.hdf5lib=$install_folder"/lib/libjhdf5.so"  -XX:NewRatio=2 -Xmx3072m -cp ./sumo.jar org.geoimage.viewer.core.batch.Sumo $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12 $13 $14 $15 $16
