# SUMO
SUMO is a Java software package to perform ship detection on satellite SAR (Synthetic Aperture Radar) images. 
It takes as main input a SAR images and its metadata, and produces as main output a list of detected ships with their attributes. 
The SUMO software is experimental. It was made for research purposes, not for operational use. 
The SUMO software implements the SUMO algorithm.

### Project Organization
SUMO is organised as a Maven project with several subprojects:

    • Sumo
        ◦ SumoCore
            It contains the classes to manage the platform configuration

        ◦ GeoImageViewer
            This project manages the GUI and in particular:
                        ▪ Image loading
                        ▪ Analysis processes
                        ▪ Import/export operations

        ◦ GeoImage
            It contains the classes to load all types of images

        ◦ GeoImageAnalysis
            It contains the algorithms to analyze the images

        ◦ SentinelXMLReader
            This project is a library used by GeoImage to read the Sentinel-1 images

        ◦ GeoLocation
            This project is a library that implements a Geolocation Algorithm. It is used 
            to convert coordinates into image pixels and back

### Installation
SUMO doesn’t need to be actually installed, it’s enough to copy the SUMO folder (downloaded from the releases page and then unzipped) on your system. 
SUMO requires JRE (or JDK) 1.7.x or above. If you don't want to install a new Java version, you could simply copy the JRE folder in your SUMO folder.

#### Test with Docker container
The Docker file in the main folder shows the steps and the configuration used to do a test with the Docker container with Ubuntu.
This method was actually used to produce the results for C. Santamaria et al. 2017 (below). 

### Run SUMO
SUMO can run in two ways, interactive mode (with GUI) and batch mode. The batch mode requires a configuration file as explained in the manual.

To run in interactive mode use start.sh for Linux and start.bat for Windows.
    
To use the batch mode use start_batch.sh for Linux and start_batch.bat for Windows.


### License
SUMO is licensed under the BSD 2-clause "Simplified" License.

### Manual
There is a basic manual and installation guide available under the project root folder. 


### Literature
The manual mentioned above: 

H. Greidanus, P. Argentieri, M. Alvarez, C. Santamaria, N. Kourti, The SUMO ship detection software for satellite radar images – Short installation and user guide, EUR KJ-01-17-945-EN-N, doi:10.2760/349278 

The SUMO algorithm is described in detail in: 

H. Greidanus, M. Alvarez, C. Santamaria, F.-X. Thoorens, N. Kourti, P. Argentieri, The SUMO Ship Detector Algorithm for Satellite Radar Images, Remote Sensing 2017, 9, 246, doi:10.3390/rs9030246 

Some results from SUMO can be found in: 

C. Santamaria, M. Alvarez, H. Greidanus, V. Syrris, P. Soille, P. Argentieri, Mass processing of Sentinel-1 images for maritime surveillance, Remote Sensing 2017, 9(7), 678, doi:10.3390/rs9070678 

H. Greidanus, C. Santamaria, M. Alvarez, D. Krause, M. Stasolla, P. W. Vachon, Non-Reporting Ship Traffic in the Western Indian Ocean, Proc. ESA Living Planet Symposium, Prague, Czech Republic, 9-13 May 2016, Ed. L. Ouwehand, SP-740, Aug 2016, ISBN 978-92-9221-305-3 

C. Santamaria, H. Greidanus, M. Fournier, T. Eriksen, M. Vespe, M. Alvarez, V. Fernandez Arguedas, C. Delaney, P. Argentieri, Sentinel-1 Contribution to Monitoring Maritime Activity in the Arctic, Proc. ESA Living Planet Symposium, Prague, Czech Republic, 9-13 May 2016, Ed. L. Ouwehand, SP-740, Aug 2016, ISBN 978-92-9221-305-3 

M. Stasolla, J.J. Mallorqui, G. Margarit, C. Santamaria, N. Walker, A comparative study of operational vessel detectors for maritime surveillance using satellite-borne Synthetic Aperture Radar, IEEE J. Sel. Top. Appl. Earth Obs. Remote Sens. 2016, 9, 2687–2701 

J. Cicuendez Perez, M. Alvarez Alvarez, J. Heikkonen, M. Indregard, Identification of bluefin tuna cages in Mediterranean Sea fishing grounds from SAR images, Int. J. Rem. Sens. 2011, 32, 4461–4474 

H. Greidanus, D. Ardill, N. Ansell, F.X. Thoorens, P. Peries, Surveying shipping and fishing in the SW Indian Ocean with satellite SAR, Proc. 33rd International Symposium on Remote Sensing of Environment, Stresa, Italy, 4–8 May 2009 

T. Lefort, F. Thoorens, H. Greidanus, M. Indregard, R. Gallagher, V. Aznar-Abian, S. Defever, N. Ramponi, Providing cost efficient near real time fisheries monitoring capability to remote and isolated areas of the globe — The case of Comoros, Proc. 33rd International Symposium on Remote Sensing of Environment, Stresa, Italy, 4–8 May 2009, pp. 833–836 

N. Kourti, I. Shepherd, H. Greidanus, M. Alvarez, E. Aresu, T. Bauna, J. Chesworth, G. Lemoine, G. Schwartz, Integrating remote sensing in fisheries control, Fish. Manag. Ecol. 2005, 12, 295–307 

H. Greidanus, P. Clayton, M. Indregard, G. Staples, N. Suzuki, P. Vachon, C. Wackerman, T. Tennvassas, J. Mallorqui, N. Kourti, R. Ringose, H. Melief, Benchmarking operational SAR ship detection, Proc. IGARSS 2004, Anchorage, AL, USA, 20–24 Sep 2004 


