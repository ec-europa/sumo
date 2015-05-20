package org.geoimage.impl.s1;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;

import jrc.it.annotation.reader.jaxb.BurstType;
import jrc.it.safe.reader.xpath.object.wrapper.BurstInformation;
import jrc.it.xml.wrapper.SumoAnnotationReader;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;


class BurstSupport{
	public String[] firstValidSamples;
	public String[] lastValidSamples;
	public int byteOffset;
	public int byteStartBurstOffset;
	public int byteEndBurstOffset;
	public int byteEndOffset;
	public byte[] databurst;
}

public class SentinelDeburstUtil {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(SentinelDeburstUtil.class);
	SumoJaxbSafeReader safeReader=null;
	SumoAnnotationReader annotation=null;
	BurstInformation burst;
	int linePerBurst=0;
	int samplePerBurst=0;
	BurstSupport[] support=null;
	byte tiffheader[];
	int linesToRemove=0;
	
	
	ImageTypeSpecifier type=null;
	
	byte endbuffer[];
	TIFFDirectory ifd=null;
	
	SentinelDeburstUtil(SumoJaxbSafeReader safeReader,SumoAnnotationReader annotation){
		init(safeReader, annotation);
	}
	
	public void init(SumoJaxbSafeReader safeReader,SumoAnnotationReader annotation){
			this.annotation=annotation;
			burst=annotation.getBurstInformation();
			linePerBurst=burst.getLinePerBust();
			samplePerBurst=burst.getSamplePerBust();
	}
	
	/**
	 * read the bursts information from the annotation file
	 * 
	 */
	public void readBurstInformation(){
		List<BurstType> list=burst.getBurstList();
		support=new BurstSupport[list.size()];
		int i=0;
		for(BurstType burst:list){
			support[i]=new BurstSupport();
			support[i].byteOffset=burst.getByteOffset().getValue().intValue();
			support[i].firstValidSamples=burst.getFirstValidSample().getValue().split(" ");
			support[i].lastValidSamples=burst.getLastValidSample().getValue().split(" ");
			linesToRemove=linesToRemove+support[i].firstValidSamples.length+support[i].lastValidSamples.length;
			System.out.println(support[i].firstValidSamples);
			//logger.debug(support[i].firstValidSamples);
			i++;
		}
		logger.debug("Burst elements informations: "+support.length);
	}
	
	

	/**
	 * calculate the offset bytes to remove from the begin and at the end
	 */
	public void processBurstInformation(){
		for(int i=0;i<support.length;i++){
			String[] fvSamples=support[i].firstValidSamples;
			for(int valid=0;valid<fvSamples.length;valid++){
				int firstVs=Integer.parseInt(fvSamples[valid]);
				
				if(firstVs==-1){
					support[i].byteStartBurstOffset=support[i].byteStartBurstOffset+4*samplePerBurst;
					support[i].byteEndBurstOffset  =support[support.length-i-1].byteStartBurstOffset+4*samplePerBurst;
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param imagePath
	 */
    public void filterBlackbands(String imagePath) {
    	RandomAccessFile fss=null;
        try {
        	File fimg = new File(imagePath);
        	fss = new RandomAccessFile(fimg.getAbsolutePath(), "r");
			
        	//leggo l'header        	
        	tiffheader=new byte[8];
        	fss.read(tiffheader);
        	
        	//int size=(samplePerBurst*4*linePerBurst);
        	for(int i=0;i<support.length;i++){
            	//salto le righe non utilizzate
            	fss.seek(support[i].byteOffset+support[i].byteStartBurstOffset);
            	        		
        		//leggo il burst togliendo le righe non utilizzate
        		int size=(samplePerBurst*4*linePerBurst)-(support[i].byteStartBurstOffset+support[i].byteEndBurstOffset);
        		support[i].databurst=new byte[size];
        		fss.read(support[i].databurst);

        	}
     
        	//read the end of the file
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        	fss.seek(support.length*samplePerBurst*4*linePerBurst);
        	
        	
        	byte bb[]=new byte[1024];
        	int res=fss.read(bb);
        	outputStream.write(bb);
        	while(res!=-1){
        		outputStream.write(bb);
        		res=fss.read(bb);
        	}
        	endbuffer=outputStream.toByteArray();
        	outputStream.close();
        	
        	
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }finally{
        	try {
				fss.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
        }
    }
	
	
    /**
	 * 
	 * @param imagePath
	 */
    public void copy(String imagePath) {
    	RandomAccessFile fss=null;
    	FileOutputStream fos = null;
        try {
        	File fimg = new File(imagePath);
        	fss = new RandomAccessFile(fimg.getAbsolutePath(), "r");
        	fos = new FileOutputStream(new File("C:\\tmp\\copy.tiff"));
        	byte buffer[]=new byte[4096];
        	
        	int res=fss.read(buffer);
        	while(res!=-1){
        		fos.write(buffer);
        		res=fss.read(buffer);
        	}
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }finally{
        	try {
				fss.close();
				fos.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
        }
    }
    
    /**
	 * 
	 * @param imagePath
	 */
    public void mergeburst() {
    	
    	FileOutputStream fos = null;
    	try{
    		fos = new FileOutputStream(new File("C:\\tmp\\test.tiff"));
    		fos.write(tiffheader);
    		
    		for(int i=0;i<support.length;i++){
    			//fos.write(support[i].header);
   				fos.write(support[i].databurst);
    		}	
    		fos.write(endbuffer);
    		
    		
    		
    	}catch (Exception ex) {
    		logger.error(ex.getMessage(),ex);
    	}finally{	
    		try {
				fos.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}	
    }
    

    /**
   	 * 
   	 * @param imagePath
   	 */
    public void splitburst() {
       	FileOutputStream fos = null;
       	for(int i=0;i<support.length;i++){
       		try{
       			try{
       				fos = new FileOutputStream(new File("C:\\tmp\\test"+i+".tiff"));
       				fos.write(support[i].databurst);
       			}finally{	
       				fos.close();
       			}	
   	    	}catch (Exception ex) {
   	    		logger.error(ex.getMessage(),ex);
   	    	} 	
           }
           
       }
	
	public void readImageWithGDal(String imagePath){
		File f=new File(imagePath);
        Dataset dataset=gdal.Open(f.getAbsolutePath() , gdalconstConstants.GA_ReadOnly);
        Band o=(Band)dataset.GetRasterBand(1);
        long flen=o.getXSize()*o.getYSize();

        byte[] raster=new byte[(o.getXSize()*o.getYSize())];
        o.ReadRaster(0,0,o.getXSize(),o.getYSize(), raster);
        byte[] preloadedDataSLC=raster;
	}
	
	public void readImageMetadata(String imagePath){
		SeekableStream fileSeekableStream;
		try {
			/* Take the input from a file */
			fileSeekableStream = new FileSeekableStream(new File(imagePath));
			FileImageInputStream imageInputStream = new FileImageInputStream(new File(imagePath));
			
			/* create ImageDecoder to count your pages from multi-page tiff */
			ImageDecoder iDecoder = ImageCodec.createImageDecoder("tiff", fileSeekableStream, null);
			
			/* count the number of pages inside the multi-page tiff */
			int pageCount = iDecoder.getNumPages();
			
			ImageReader imageReader = ImageIO.getImageReadersByFormatName("tif").next();
			imageReader.setInput(imageInputStream);
			/* use first for loop to get pages one by one */
			for(int page = 0; page < pageCount; page++){
				/* get image metadata for each page */
				IIOMetadata imageMetadata = imageReader.getImageMetadata(page);
	
				/*
				 * The root of all the tags for this image is the IFD (Image File Directory).
				 * Get the IFD from where we can get all the tags for the image.
				 */
				ifd = TIFFDirectory.createFromMetadata(imageMetadata);
				
				/* Create a Array of TIFFField*/
				TIFFField[] allTiffFields = ifd.getTIFFFields();
				
				int count=0;
				/* use second for loop to get all field data */
				for (int i = 0; i < allTiffFields.length; i++) {
					TIFFField tiffField = allTiffFields[i];
		
					/* name of property */
					String nameOfField = tiffField.getTag().getName();
					/* Type of property (optional) */
					String typeOfField = TIFFField.getTypeName(tiffField.getType());                                 
					/* Tag no. of the property (optional) */
					int numberOfField = tiffField.getTagNumber();
					count=count+tiffField.getTagNumber();	
					System.out.println("nameOfField:"+nameOfField + " --  typeOfField:"+typeOfField+ "  -- numberOfField:"+numberOfField);
				}
				// we are looking for 259, jump optional step out of loop
				TIFFField compField = ifd.getTIFFField(259);
				/* Value of Property*/
				int valueOfField = compField.getAsInt(0);
				System.out.println("");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	
	
	public SumoJaxbSafeReader getSafeReader() {
		return safeReader;
	}

	public void setSafeReader(SumoJaxbSafeReader safeReader) {
		this.safeReader = safeReader;
	}

	public SumoAnnotationReader getAnnotation() {
		return annotation;
	}

	public void setAnnotation(SumoAnnotationReader annotation) {
		this.annotation = annotation;
	}

	public BurstInformation getBurst() {
		return burst;
	}

	public void setBurst(BurstInformation burst) {
		this.burst = burst;
	}

	public int getLinePerBurst() {
		return linePerBurst;
	}

	public void setLinePerBurst(int linePerBurst) {
		this.linePerBurst = linePerBurst;
	}

	public int getSamplePerBurst() {
		return samplePerBurst;
	}

	public void setSamplePerBurst(int samplePerBurst) {
		this.samplePerBurst = samplePerBurst;
	}

	public BurstSupport[] getSs() {
		return support;
	}

	public int getLinesToRemove() {
		return linesToRemove;
	}

	public void setLinesToRemove(int linesToRemove) {
		this.linesToRemove = linesToRemove;
	}
	/*
	
	public static void main(String args[]){
		String pathTiff ="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//measurement//s1a-iw1-slc-hh-20140502t170314-20140502t170342-000421-0004cc-001.tiff";
		String pathXml ="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//annotation//s1a-iw1-slc-hh-20140502t170314-20140502t170342-000421-0004cc-001.xml";
		String pathSafe ="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//manifest.safe";
		
		
		SentinelDeburstingTest s1=new SentinelDeburstingTest(pathSafe,pathXml);
		s1.readImageMetadata(pathTiff);
		
		s1.readBurstInformation();
		s1.processBurstInformation();
		s1.filterBlackbands(pathTiff);
		s1.copy(pathTiff);
		s1.mergeburst();
		System.out.println("");
	}*/
	
	
	
	
	/*
	public void readDeburstImage(String imagePath){
		
		RenderedImage img =  (RenderedImage)JAI.create("fileload", imagePath);
		
		int width = img.getWidth(); // Dimensions of the image
		System.out.println("Width:"+width);
		int height= img.getHeight();
		System.out.println("height:"+height);
		
		// Let's create a BufferedImage for a gray level image.
		// We need its raster to set the pixels' values.
		Raster raster = img.getData();

		int numDataElements=raster.getNumDataElements();
		System.out.println("Num data elements:"+numDataElements);
		
		//int sizeBuffer=raster.getDataBuffer().getSize();
		List<Integer> data=new ArrayList<Integer>();
		int indexValidSamples=0;
		for(int y=0;y<height;y++){
			if(firstValidSamples[indexValidSamples]!=-1){
				for(int x=0;x<width;x++){
					data.add(raster.getSample(x, y, 0));
					//indexValidSamples=x+x*y;
				}
			}
			indexValidSamples++;
		}
		
		int[] values=ArrayUtils.toPrimitive(data.toArray(new Integer[data.size()]));
		ByteBuffer byteBuffer = ByteBuffer.allocate(values.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(values);
        byte[] array = byteBuffer.array();
		
		System.out.println("");
	}*/
}




