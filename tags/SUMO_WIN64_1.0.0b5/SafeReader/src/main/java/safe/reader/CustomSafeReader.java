package safe.reader;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class CustomSafeReader {
	private File safe=null;
	
	private final String LAST_FOLDER="LAST_FOLDER";
	JFrame frame;
	Preferences pref;
	
	
	public String selectFile(){
		String filePath=null;
		String folder=(String)pref.get(LAST_FOLDER,"");
		JFileChooser chooser=new JFileChooser(folder);
		int returnVal=chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filePath=file.getAbsolutePath();
            pref.put(LAST_FOLDER, file.getParentFile().getAbsolutePath());	
        }
		return filePath;
	}
	
	public void parseProductXML(File productxml) throws TransformerException {
        try {
           /* SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(productxml);
            Element atts = doc.getRootElement().getChild("productInfo");
            setMetadata(SATELLITE, new String("TerraSAR-X"));
            setMetadata(SENSOR, atts.getChild("acquisitionInfo").getChild("sensor").getText());
            String pols = "";
            for (Object o : atts.getChild("acquisitionInfo").getChild("polarisationList").getChildren("polLayer")) {
                Element elem = (Element) o;
                pols = pols + elem.getText()+" ";
                //bands.add(elem.getText());
            }
            pols.substring(0, pols.length()-1);
            setMetadata(POLARISATION, pols);

            setMetadata(LOOK_DIRECTION, atts.getChild("acquisitionInfo").getChild("lookDirection").getText());
            setMetadata(MODE, atts.getChild("acquisitionInfo").getChild("imagingMode").getText());


            setMetadata(PRODUCT, atts.getChild("productVariantInfo").getChild("productType").getText());
            setMetadata(ORBIT_DIRECTION, atts.getChild("missionInfo").getChild("orbitDirection").getText());
            setMetadata(HEIGHT, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfRows").getText());
            setMetadata(WIDTH, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfColumns").getText());
            setMetadata(RANGE_SPACING, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("rowSpacing").getText());
            setMetadata(AZIMUTH_SPACING, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("columnSpacing").getText());

            setMetadata(NUMBER_BYTES, new Integer(atts.getChild("imageDataInfo").getChild("imageDataDepth").getText()) / 8);
            setMetadata(ENL, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("azimuthLooks").getText());
            xSize = new Integer(getMetadata(WIDTH).toString());
            ySize = new Integer(getMetadata(HEIGHT).toString());
            setMetadata(HEADING_ANGLE, atts.getChild("sceneInfo").getChild("headingAngle").getText());
            rangeTimeStart = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("firstPixel").getText());
            rangeTimeStop = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("lastPixel").getText());
            String time = atts.getChild("sceneInfo").getChild("start").getChild("timeUTC").getText();
            time = time.substring(0, time.lastIndexOf("."));
            setMetadata(TIMESTAMP_START, Timestamp.valueOf(time.replaceAll("T", " ")));
            time = atts.getChild("sceneInfo").getChild("stop").getChild("timeUTC").getText();
            time = time.substring(0, time.lastIndexOf("."));
            setMetadata(TIMESTAMP_STOP, Timestamp.valueOf(time.replaceAll("T", " ")));
            // calculate satellite speed using state vectors
            atts = doc.getRootElement().getChild("platform").getChild("orbit").getChild("stateVec");
            double xvelocity = Double.valueOf(atts.getChildText("velX"));
            double yvelocity = Double.valueOf(atts.getChildText("velY"));
            double zvelocity = Double.valueOf(atts.getChildText("velZ"));
            double satellite_speed = Math.sqrt(xvelocity * xvelocity + yvelocity * yvelocity + zvelocity * zvelocity);
            setMetadata(SATELLITE_SPEED, String.valueOf(satellite_speed));
            xposition = Double.valueOf(atts.getChildText("posX"));
            yposition = Double.valueOf(atts.getChildText("posY"));
            zposition = Double.valueOf(atts.getChildText("posZ"));

            float radarFrequency = new Float(doc.getRootElement().getChild("instrument").getChild("radarParameters").getChild("centerFrequency").getText());
            setMetadata(RADAR_WAVELENGTH, String.valueOf(299792457.9 / radarFrequency));

            setMetadata(SATELLITE_ORBITINCLINATION, "97.44");
            setMetadata(REVOLUTIONS_PERDAY, String.valueOf(11));


            //metadata used for ScanSAR mode during the Azimuth ambiguity computation
            if (getMetadata(MODE).equals("SC")) {
                //extraction of the 4 PRF codes
                int prf_count = 1;
                for (Object o : doc.getRootElement().getChild("instrument").getChildren("settings")) {
                    Element elem = (Element) o;
                    setMetadata("PRF" + prf_count, elem.getChild("settingRecord").getChild("PRF").getText());
                    prf_count++;
                }
                
                */
                
                /*double prf_average = 0;
                for (int prf = 1; prf < prf_count; prf++) {
                prf_average = prf_average + new Double((String) getMetadata(PRF+ prf));
                }
                prf_average = prf_average / (prf_count-1);
                setMetadata(PRF, String.valueOf(prf_average));*/
        	
        	
        	/*
                setMetadata(PRF, ""); //to recognise the TSX SC in the azimuth computation

                //the SC mode presents 4 strips which overlap, the idea is to consider one strip till the middle of the overlap area
                int b = 1;
                for (Object o : doc.getRootElement().getChild("processing").getChildren("processingParameter")) {
                    if (b == 4) {
                        continue;
                    }
                    Element elem = (Element) o;
                    double start_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStart").getText());
                    double stop_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStop").getText());
                    double aver_range_time = start_range_time + (stop_range_time - start_range_time) / 2;

                    int stripBound = new Double(((aver_range_time - rangeTimeStart) * xSize) / (rangeTimeStop - rangeTimeStart)).intValue();
                    setMetadata("STRIPBOUND" + b++, new Integer(stripBound).toString());
                }
            }

            setMetadata(K, doc.getRootElement().getChild("calibration").getChild("calibrationConstant").getChild("calFactor").getText());

            //row and cols of the mapping_grid table used for geolocation
            MGRows = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("numberOfRows").getText());
            MGCols = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("numberOfColumns").getText());
            MGRefRow = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("refRow").getText());
            MGRefCol = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("refCol").getText());
            MGRowSpacing = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("rowSpacing").getText());
            MGColSpacing = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("columnSpacing").getText());
            String MGtTimes = doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("tReferenceTimeUTC").getText();
            MGtTimes = MGtTimes.substring(0, MGtTimes.length() - 1);
            MGtTime = Timestamp.valueOf(MGtTimes.replaceAll("T", " ")).getTime();
            MGtauTime = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("tauReferenceTime").getText());
            ImageRowSpacing = Double.valueOf(doc.getRootElement().getChild("productInfo").getChild("imageDataInfo").getChild("imageRaster").getChild("rowSpacing").getText());
            ImageColSpacing = Double.valueOf(doc.getRootElement().getChild("productInfo").getChild("imageDataInfo").getChild("imageRaster").getChild("columnSpacing").getText());


        } catch (JDOMException ex) {
            Logger.getLogger(TerrasarXImage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TerrasarXImage.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
