package org.geoimage.impl.s1;

import static org.junit.Assert.*;

import java.io.IOException;

import org.geoimage.factory.GeoImageReaderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Sentinel1GRDTest {
	private static Sentinel1 reader=null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		reader=(Sentinel1)GeoImageReaderFactory.createReaderForName(
				"\"H:\\sat\\geocoding img with problems\\S1A_EW_GRDM_1SDH_20150105T060017_20150105T060121_004032_004DC8_BE6F.SAFE\"");
		reader.initialise();
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
	public void testRead() {
		int[] data=null;
			data = reader.read(10, 10, 10,10, 0);
		assertNotNull(data);
		
	}

	@Test
	public void testReadAndDecimateTileIntIntIntIntDoubleBooleanIProgressInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOverviewFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadTile() {
		fail("Not yet implemented");
	}

	@Test
	public void testPreloadLineTile() {
		fail("Not yet implemented");
	}

	@Test
	public void testSentinel1GRD() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadTileXY() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNumberOfBytes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSwath() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSwath() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImgName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDisplayName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBands() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHeight() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAmbiguityCorrection() {
		fail("Not yet implemented");
	}

	@Test
	public void testDispose() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetGcps() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalcSatelliteSpeed() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPRFIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSentinel1StringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSentinel1StringFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNBand() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIpfVersion() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetIpfVersion() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInstumentationMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetInstumentationMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAccessRights() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFilesList() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitialise() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSwathName() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBandName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTypeBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFormat() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInternalImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSafeFilePath() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImage() {
		fail("Not yet implemented");
	}

}
