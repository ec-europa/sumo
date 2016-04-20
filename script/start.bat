SET GDAL_DATA=%CD%\lib
SET GDAL_DRIVER_PATH=%CD%\lib
SET LD_LIBRARY_PATH=%CD%\lib

SET PATH=%PATH%;%CD%\lib
.\jre\bin\java -Dlog4j.configurationFile=log4j2.xml -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:NewRatio=2 -Xmx3072m -Djava.library.path="%CD%/lib/" -Dncsa.hdf.hdf5lib.H5.hdf5lib="%CD%/lib/jhdf5dll-2.11.0.dll" -jar sumo.jar

