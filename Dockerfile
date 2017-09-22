#=================================================================
# Dockerfile of SUMO based on ubuntu:xenial
# - version: 1.0
# - modified: 2017-03-06 
# Generate it:
#	sudo docker build -t marsec:java1.8_gdalext_SUMO_Lin64_1.3.5 .
#
# This Dockerfile was used as basis to perform on the JRC Earth Observation
# Data and Processing Platform (JEODPP) the experiments reported in 
# https://dx.doi.org/10.3390/rs907067 
#
# @Article{     santamaria-etal2017rs,
#   author      = {Santamaria, C. and Alvarez, M. and Greidanus, H. and
#                 Syrris, V. and Soille, P. and Argentieri, P.},
#   title       = {Mass processing of {S}entinel-1 images for maritime
#                 surveillance},
#   journal     = {Remote Sensing},
#   year        = {2017},
#   doi         = {10.3390/rs9070678},
#   volume      = {9},
#   number      = {7},
#   pages       = {678/1-678/20}
# }
#
# JEODPP concept paper: http://publications.jrc.ec.europa.eu/repository/bitstream/JRC98089/soille-etal2016bids.pdf
#
# JEODPP web site: https://cidportal.jrc.ec.europa.eu/home/
#
# (c) European Union, 2017
#=================================================================

FROM ubuntu:xenial

LABEL project="MARSEC" \
      author="EO&SS_Big_Data" \
      image_name="java1.8_gdalext_SUMO_Lin64_1.3.5" \
      version="1.0" \
      released="2017-03-06" \
      software_versions="java1.8, gdal extensions, SUMO (version Lin64_1.3.5)" \
      description="MARSEC (MARitime SECurity): the maritime surveillance and security from EU to Global"

# Ubuntu package installation
RUN echo "Updating package list and packages"
RUN apt-get update && apt-get install --assume-yes apt-utils && apt-get -f install -y && apt-get install -y apt-file && apt-get upgrade -y 

# Basics
RUN apt-get install -y build-essential

# Misc
RUN apt-get install -y wget && apt-get install -y htop && apt-get install -y psmisc && apt-get install -y vim && apt-get install -y sudo && apt-get install -y unzip && apt-get install -y mlocate && updatedb
RUN apt-get install -y libjson0 && apt-get install -y libjson0-dev

# Install Java 8
RUN apt-get install -y software-properties-common && add-apt-repository ppa:webupd8team/java && apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
	&& apt-get install -y oracle-java8-installer && apt-get install -y oracle-java8-set-default
ENV JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre

# Download SUMO software
RUN mkdir -p /usr/local/share/SUMO && cd /usr/local/share/SUMO && \
    wget https://github.com/ec-europa/sumo/releases/download/SUMO64_1.3.5/SUMO_Lin64_1.3.5.zip && \
	unzip SUMO_Lin64_1.3.5.zip && rm SUMO_Lin64_1.3.5.zip

# GDAL extensions
# http://docs.geoserver.org/latest/en/user/data/raster/gdal.html
# http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.15/native/gdal/
RUN cd /usr/local/share/SUMO && mkdir gdal_extensions && cd gdal_extensions \
	&& wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.15/native/gdal/linux/gdal192-Ubuntu12-gcc4.6.3-x86_64.tar.gz \
	&& tar zxvf gdal192-Ubuntu12-gcc4.6.3-x86_64.tar.gz
ENV LD_LIBRARY_PATH=/usr/local/share/SUMO/gdal_extensions/
RUN cd /usr/local/share/SUMO/gdal_extensions && wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.15/native/gdal/gdal-data.zip \
	&& unzip gdal-data.zip
ENV GDAL_DATA=/usr/local/share/SUMO/gdal_extensions/gdal-data
RUN rm /usr/local/share/SUMO/gdal_extensions/gdal-data.zip && rm /usr/local/share/SUMO/gdal_extensions/gdal192-Ubuntu12-gcc4.6.3-x86_64.tar.gz
ENV PATH=$PATH:/usr/local/share/SUMO/SUMO_Lin64_1.3.5

RUN sudo updatedb

RUN cd /usr/local/share/SUMO
	
CMD /bin/bash

# Inside the container, set the following "env" variables:
# export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
# export LD_LIBRARY_PATH=/usr/local/share/SUMO/gdal_extensions/
# export GDAL_DATA=/usr/local/share/SUMO/gdal_extensions/gdal-data
# export PATH=$PATH:/usr/local/share/SUMO/SUMO_Lin64_1.3.5
