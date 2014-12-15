package org.geoimage.viewer.core.batch;

import static org.junit.Assert.*;

import org.geoimage.viewer.core.batch.MultipleBatchAnalysis;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultipleBatchAnalysisTest {
	private static MultipleBatchAnalysis mBatch;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AnalysisParams params=new AnalysisParams();
		
		params.shapeFile="C:\\tmp\\land polygon opsm 50m\\land_grid_buffer_50m.shp";
		params.thresholdArrayValues=new int[]{5,5,5,5};
		params.epsg="C:\\tmp\\output\\";
		params.buffer=10;
		params.pathImg="C:\\tmp\\input"; 			//input folder
		params.epsg="C:\\tmp\\testmultiple\\";
		
		ConfigurationFile conf=new ConfigurationFile(null);
		
		mBatch=new MultipleBatchAnalysis(params,null);
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
	public void testStartAnalysis() {
		fail("Not yet implemented");
	}

	@Test
	public void testMultipleBatchAnalysis() {
		fail("Not yet implemented");
	}

	@Test
	public void testAbstractBatchAnalysis() {
		fail("Not yet implemented");
	}

	@Test
	public void testRunProcess() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadShapeFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testAnalizeImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveResults() {
		fail("Not yet implemented");
	}

}
