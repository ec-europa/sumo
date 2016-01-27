# SET GDAL_DATA="C:/Workspaces_MyEclipse Professional 2014/AlosReader/gdalbin/bin/gdal-data"
# SET GDAL_DRIVER_PATH="C:/Workspaces_MyEclipse Professional 2014/AlosReader/gdalbin/bin/gdal/plugin"
# SET LD_LIBRARY_PATH="C:/Workspaces_MyEclipse Professional 2014/AlosReader/gdalbin/bin/gdal/java"
java -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:NewRatio=2 -Xmx3072m -Djava.library.path="%CD%/lib/gdal_lib_win64/bin/gdal/java;%CD%/lib/gdal_lib_win64/bin/gdal/plugins/" -Dncsa.hdf.hdf5lib.H5.hdf5lib="%CD%/lib/jhdf5dll-2.11.0.dll" -jar sumo.jar
