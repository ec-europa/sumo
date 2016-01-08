SET PATH=%PATH%;%CD%\sumo_lib\gdal_lib_win64;%CD%\sumo_lib\gdal_lib_win64\gdal\plugins
SET GDAL_DRIVER_PATH=%CD%\sumo_lib\gdal_lib_win64
.\jre\bin\java -XX:+UseParNewGC -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:NewRatio=2 -Xmx3072m -Djava.library.path="./;%CD%/sumo_lib/;%CD%\sumo_lib\hdf5\jarhdf5-2.10.1.jar;%CD%/sumo_lib/gdal_lib_win64/;%CD%/sumo_lib/gdal_lib_win64/gdal/java/;%CD%/sumo_lib/gdal_lib_win64/gdal/plugins/" -Dncsa.hdf.hdf5lib.H5.hdf5lib="%CD%\sumo_lib\hdf5\jhdf5.dll"  -jar sumo.jar

