package org.geoimage.def;

import java.util.HashMap;

import org.jdom.Element;
import org.jdom.Namespace;

public abstract class SUMOMetadata implements GeoMetadata,SarMetadata{
	private HashMap<String, Object> metadata = new HashMap<String, Object>();
	
	
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	protected  void setMetadata(String key, Object value){
		metadata.put(key, value);
	}
    
	/**
	 * 
	 * @param key
	 * @return
	 */
	protected Object getMetadata(String key){
    	return metadata.get(key);
    }
	
	
		
	
	/**
	 * 	
	 * @param atts
	 * @param attribute
	 * @param metadata
	 * @param namespace
	 * @param type
	 * @return
	 */
	 protected Object setMetadataXML(Element atts, String attribute, String metadata, Namespace namespace, Class<?> type) {
    	Element o=atts.getChild(attribute, namespace);
    	String value=null;
    	Object val=null;
        if((atts != null) && (o != null)){
        	value=o.getText();
        	if(type==String.class){
        		val=value;
        		setMetadata(metadata,(String)value);
        	}else if(type==Integer.class){
        		val=Integer.parseInt(value);
        		setMetadata(metadata, val);
        	}else if(type==Double.class){
        		val=Double.parseDouble(value);
        		setMetadata(metadata, val);
        	}else if(type==Float.class){
        		val=Float.parseFloat(value);
        		setMetadata(metadata, val);
        	}	
        }
        return val;
    }
	
	
    public String getTimeStampStart(){
    	return (String)getMetadata(GeoMetadata.TIMESTAMP_START);
    }
    public void setTimeStampStart(String data){
    	setMetadata(GeoMetadata.TIMESTAMP_START,data);
    }
    
    public String getTimeStampStop(){
    	return (String)getMetadata(GeoMetadata.TIMESTAMP_STOP);
    }
    public void setTimeStampStop(String data){
    	setMetadata(GeoMetadata.TIMESTAMP_STOP,data);
    }
    
    public int getNumberOfBytes(){
    	return (Integer)getMetadata(GeoMetadata.NUMBER_BYTES);
    }
    public void setNumberOfBytes(Integer n){
    	setMetadata(GeoMetadata.NUMBER_BYTES,n);
    }
    
    public Double getHeadingAngle(){
    	return (Double)getMetadata(GeoMetadata.HEADING_ANGLE);
    }
    public void setHeadingAngle(Double data){
    	setMetadata(GeoMetadata.HEADING_ANGLE,data);
    }
    
    public abstract String getSensor();/*{
    	return (String)getMetadata(GeoMetadata.SENSOR);
    }*/
    public void setSensor(String data){
    	setMetadata(GeoMetadata.SENSOR,data);
    }
    
    public String getLookDirection(){
    	return (String)getMetadata(GeoMetadata.LOOK_DIRECTION);
    }
    public void setLookDirection(String data){
    	setMetadata(GeoMetadata.LOOK_DIRECTION,data);
    }
    
    public String getOrbitDirection(){
    	return (String)getMetadata(GeoMetadata.ORBIT_DIRECTION);
    }
    public void setOrbitDirection(String data){
    	setMetadata(GeoMetadata.ORBIT_DIRECTION,data);
    }
    
    public String getSatellite(){
    	return (String)getMetadata(GeoMetadata.SATELLITE);
    }
    public void setSatellite(String data){
    	setMetadata(GeoMetadata.SATELLITE,data);
    }
    
    public String getProcessor(){
    	return (String)getMetadata(GeoMetadata.PROCESSOR);
    }
    public void setProcessor(String data){
    	setMetadata(GeoMetadata.PROCESSOR,data);
    }
    
    
    public int getNumberBands(){
    	return (Integer)getMetadata(GeoMetadata.NUMBER_BANDS);
    }
    public void setNumberBands(Integer data){
    	setMetadata(GeoMetadata.NUMBER_BANDS,data);
    }
    
    public Float getIncidenceNear(){
    	return (Float)getMetadata(GeoMetadata.INCIDENCE_NEAR);
    }
    public void setIncidenceNear(Float data){
    	setMetadata(GeoMetadata.INCIDENCE_NEAR,data);
    }
    
    public Float getIncidenceFar(){
    	return (Float)getMetadata(GeoMetadata.INCIDENCE_FAR);
    }
    public void setIncidenceFar(Float data){
    	setMetadata(GeoMetadata.INCIDENCE_FAR,data);
    }
    
    public double getSlantRangeNearEdge(){
    	return (Double)getMetadata(GeoMetadata.SLANT_RANGE_NEAR_EDGE);
    }
    public void setSlantRangeNearEdge(Double data){
    	setMetadata(GeoMetadata.SLANT_RANGE_NEAR_EDGE,data);
    }

    public Float getAzimuthSpacing(){
    	return (Float)getMetadata(GeoMetadata.AZIMUTH_SPACING);
    }
    public void setAzimuthSpacing(Float data){
    	setMetadata(GeoMetadata.AZIMUTH_SPACING,data);
    }

    public Float getRangeSpacing(){
    	return (Float)getMetadata(GeoMetadata.RANGE_SPACING);
    }
    public void setRangeSpacing(Float data){
    	setMetadata(GeoMetadata.RANGE_SPACING,data);
    }
    
    
    public double getSatelliteAltitude(){
    	try{
    		return (Double)getMetadata(GeoMetadata.SATELLITE_ALTITUDE);
    	}catch(Exception e){
    		return 0;
    	}
    	
    }
    public void setSatelliteAltitude(Double data){
    	setMetadata(GeoMetadata.SATELLITE_ALTITUDE,data);
    }
    
    public double getSatelliteOrbitInclination(){
    	return (Double)getMetadata(GeoMetadata.SATELLITE_ORBITINCLINATION);
    }
    public void setSatelliteOrbitInclination(Double data){
    	setMetadata(GeoMetadata.SATELLITE_ORBITINCLINATION,data);
    }
    
    public String getSimpleTimeOrdering(){
    	return (String)getMetadata(GeoMetadata.SIMPLE_TIME_ORDERING);
    }
    public void getSimpleTimeOrdering(String data){
    	setMetadata(GeoMetadata.SIMPLE_TIME_ORDERING,data);
    }
    
    
    public double getMajorAxis(){
    	return (Double)getMetadata(GeoMetadata.MAJOR_AXIS);
    }
    public void setMajorAxis(Double data){
    	setMetadata(GeoMetadata.MAJOR_AXIS,data);
    }
    
    public double getMinorAxis(){
    	return (Double)getMetadata(GeoMetadata.MINOR_AXIS);
    }
    public void setMinorAxis(Double data){
    	setMetadata(GeoMetadata.MINOR_AXIS,data);
    }
    
    public double getGeodeticTerraHeight(){
    	return (Double)getMetadata(GeoMetadata.GEODETIC_TERRA_HEIGHT);
    }
    public void setGeodeticTerraHeight(Double data){
    	setMetadata(GeoMetadata.GEODETIC_TERRA_HEIGHT,data);
    }
    
    public String getSwath(){
    	return (String)getMetadata(GeoMetadata.SWATH);
    }
    public void setSwath(String data){
    	setMetadata(GeoMetadata.SWATH,data);
    }
    

    public double getRevolutionsPerday(){
    	return (Double)getMetadata(GeoMetadata.REVOLUTIONS_PERDAY);
    }
    public void setRevolutionsPerday(double data){
    	setMetadata(GeoMetadata.REVOLUTIONS_PERDAY,data);
    }
    

    public Double getSatelliteSpeed(){
    	Object o=getMetadata(GeoMetadata.SATELLITE_SPEED);
    	if(o!=null)
    		return (Double)o;
    	return null;
    }
    public void setSatelliteSpeed(double data){
    	setMetadata(GeoMetadata.SATELLITE_SPEED,data);
    }
    
    public int getMetaWidth(){
    	return (Integer)getMetadata(GeoMetadata.WIDTH);
    }
    public void setMetaWidth(Integer data){
    	setMetadata(GeoMetadata.WIDTH,data);
    }
    
    public int getMetaHeight(){
    	return (Integer)getMetadata(GeoMetadata.HEIGHT);
    }
    public void setMetaHeight(Integer data){
    	setMetadata(GeoMetadata.HEIGHT,data);
    }
    
    public String getType(){
    	return (String)getMetadata(GeoMetadata.TYPE);
    }
    public void setType(String data){
    	setMetadata(GeoMetadata.TYPE,data);
    }
    
    
    ////////////////////////////////////////////		Sar metadata		 /////////////////////
    
    
    public String getMode(){
    	return (String)getMetadata(SarMetadata.MODE);
    }
    public void setMode(String data){
    	setMetadata(SarMetadata.MODE,data);
    }
    
    
    public String getBeam(){
    	return (String)getMetadata(SarMetadata.BEAM);
    }
    public void setBeam(String data){
    	setMetadata(SarMetadata.BEAM,data);
    }
    
    public String getProduct(){
    	return (String)getMetadata(SarMetadata.PRODUCT);
    }
    public void setProduct(String data){
    	setMetadata(SarMetadata.PRODUCT,data);
    }
    
    
    public String getENL(){
    	return (String)getMetadata(SarMetadata.ENL);
    }
    public void setENL(String data){
    	setMetadata(SarMetadata.ENL,data);
    }
    
    
    public String getPolarization(){
    	return (String)getMetadata(SarMetadata.POLARISATION);
    }
    public void setPolarization(String data){
    	setMetadata(SarMetadata.POLARISATION,data);
    }
    
    
    public double getRadarWaveLenght(){
    	return (Double)getMetadata(SarMetadata.RADAR_WAVELENGTH);
    }
    public void setRadarWaveLenght(double data){
    	setMetadata(SarMetadata.RADAR_WAVELENGTH,data);
    }
    
    
    public Double getPRF(){
    	return (Double)getMetadata(SarMetadata.PRF);
    }
    public void setPRF(Double data){
    	setMetadata(SarMetadata.PRF,data);
    }
    
    
    public double getPRF1(){
    	return (Double)getMetadata(SarMetadata.PRF1);
    }
    public void setPRF1(double data){
    	setMetadata(SarMetadata.PRF1,data);
    }
    
    
    public double getPRF2(){
    	return (Double)getMetadata(SarMetadata.PRF2);
    }
    public void setPRF2(double data){
    	setMetadata(SarMetadata.PRF2,data);
    }
    
    
    public double getPRF3(){
    	return (Double)getMetadata(SarMetadata.PRF3);
    }
    public void setPRF3(double data){
    	setMetadata(SarMetadata.PRF3,data);
    }
    
    
    public double getPRF4(){
    	return (Double)getMetadata(SarMetadata.PRF4);
    }
    public void setPRF4(double data){
    	setMetadata(SarMetadata.PRF4,data);
    }
    
    
    public int getStripBound1(){
    	return (Integer)getMetadata(SarMetadata.STRIPBOUND1);
    }
    public void setStripBound1(Integer data){
    	setMetadata(SarMetadata.STRIPBOUND1,data);
    }
    
    public int getStripBound2(){
    	return (Integer)getMetadata(SarMetadata.STRIPBOUND2);
    }
    public void setStripBound2(Integer data){
    	setMetadata(SarMetadata.STRIPBOUND2,data);
    }
    
    public int getStripBound3(){
    	return (Integer)getMetadata(SarMetadata.STRIPBOUND3);
    }
    public void setStripBound3(Integer data){
    	setMetadata(SarMetadata.STRIPBOUND3,data);
    }
    
    public double getK(){
    	return (Double)getMetadata(SarMetadata.K);
    }
    public void setK(Double data){
    	setMetadata(SarMetadata.K,data);
    }
    
    
    
    
}
