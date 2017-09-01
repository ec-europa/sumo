# sumo

SUMO is a Java software package to perform ship detection on satellite SAR (Synthetic Aperture Radar) images. 
It takes as main input SAR images and its metadata, and produces as main output a list of detected ships with their attributes. 
The SUMO software is experimental. It was made for research purposes, not for operational use. 
SUMO implements "the SUMO algorithm" that has been described in detail in Greidanus et al., 2017. 


### Project Organization

Sumo is organized as a maven project with several subprojects.

    • Sumo
        ◦ SumoCore
            It contains the classes to manage the platform configuration

        ◦ GeoImageViewer
            This project manage the GUI and in particular:
                        ▪ Image loading
                        ▪ Analysis processes
                        ▪ import/export  operations

        ◦ GeoImage
            It contains the classes to load all types of Images

        ◦ GeoImageAnalysis
            It contains the algorithms to analyze the images

        ◦ SentinelXMLReader
            This project is a library used by GeoImage to read the S1 images

        ◦ GeoLocation
            This project is a library that implement a Geolocation Algorithm. It is used 
            to convert coordinates into image pixel and back

### Installation
Sumo doesn’t need to be installed, It’s enough to copy the Sumo folder in your system.
SUMO require JRE (or JDK) 1.7.x or above. If you don't want to install a new Java version, you could simply copy  
the JRE folder in your SUMO folder.


### Run Sumo
Sum can run in two ways , interactive mode  ( with GUI ) and in batch mode. The batch mode require a 
configuration file as explained in the manual.

To run in interactive mode use start.sh for Linux and start.bat for Windows .
    
To use the batch mode use start_batch.sh for Linux and start_batch.bat for Windows.



### License
Sumo is licensed under the BSD 2-clause "Simplified" License


