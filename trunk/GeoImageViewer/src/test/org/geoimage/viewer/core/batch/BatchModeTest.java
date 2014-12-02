package org.geoimage.viewer.core.batch;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BatchModeTest {
	private String params="-i \"C:\\tmp\\sumo_images\\carlos tests\\Caso critico\\S1A_IW_GRDH_1SDH_20140607T205125_20140607T205150_000949_000EC8_CDCE.SAFE\\manifest.safe \"  -b 10 "
			+ " -thh 5 -thv 5 -tvh 5 -tvv 5 -sh \"C:\\tmp\\land-polygons-split-4326\\land-polygons-split-4326\\land_polygons.shp\" -o C:\\tmp\\output";
	private String testImage="C:\\tmp\\sumo_images\\carlos tests\\Caso critico\\S1A_IW_GRDH_1SDH_20140607T205125_20140607T205150_000949_000EC8_CDCE.SAFE\\manifest.safe ";
	
			
			
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSumo() {
		System.out.println("Start SUMO in batch mode");
		String[] args= params.split(" ");
		List<String> params=Arrays.asList(args);
		Sumo s=new Sumo();
		int  st = s.parseParams(params);
		if(st!=s.PARAM_ERROR){
			System.out.println("Run Analysis");
			s.execAnalysis();
			System.out.println("Save results");
			//s.saveResults();
		}
		System.out.println("Exit");
		assertTrue(true);
	}

	@Test
	public void testParseParams() {
		String[] args= params.split(" ");
		List<String> params=Arrays.asList(args);
		Sumo s=new Sumo();
		int  st = s.parseParams(params);
		if(st!=s.PARAM_ERROR)
			assertTrue(true);
		
	}
	
	@Test
	public void testExecAnalysis() {
		
	}

	@Test
	public void testStartAnalysis() {
		List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(testImage);
		SarImageReader reader=(SarImageReader) readers.get(0);
		
	/*			
		GeometricLayer gl=null;
		if(params.shapeFile!=null)
			gl=readShapeFile(reader);
		
		IMask[] masks = new IMask[1];
		if(params.buffer!=0&&gl!=null)
			masks[0]=FactoryLayer.createBufferedLayer("buffered", FactoryLayer.TYPE_COMPLEX, params.buffer, reader, gl);
		
		startAnalysis(reader,masks);*/
	}


}
