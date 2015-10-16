package org.geoimage.impl.cosmo;

import org.geoimage.factory.GeoImageReaderFactory;

import junit.framework.TestCase;

/**
 * The class <code>CosmoSkymedImageTest</code> contains tests for the class
 * {@link <code>CosmoSkymedImage</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 28/08/15 12.57
 *
 * @author argenpo
 *
 * @version $Revision$
 */
public class CosmoSkymedImageTest extends TestCase {
	private static AbstractCosmoSkymedImage reader=null;
	/**
	 * Construct new test instance
	 *
	 * @param name the test name
	 */
	public CosmoSkymedImageTest(String name) {
		super(name);
	}

	/**
	 * Launch the test.
	 *
	 * @param args String[]
	 */
	public static void main(String[] args) {
		
	}

	/**
	 * Perform pre-test initialization
	 *
	 * @throws Exception
	 *
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		reader=(AbstractCosmoSkymedImage) GeoImageReaderFactory.createReaderForName(
				"F:\\SumoImgs\\CSK\\CSKS1_DGM_B_HI_07_HH_RA_FF_20111123022856_20111123022903\\CSKS1_DGM_B_HI_07_HH_RA_FF_20111123022856_20111123022903.h5").get(0);
		reader.initialise();
	}

	/**
	 * Perform post-test clean up
	 *
	 * @throws Exception
	 *
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		reader.dispose();
	}

	/**
	 * Run the String getBandName(int) method test
	 */
	public void testGetBandName() {
		// add test code here
		int band = 0;
		String bandName=reader.getBandName(band);
		assertEquals("",bandName);
		assertTrue(true);
	}

	/**
	 * Run the String[] getBands() method test
	 */
	public void testGetBands() {
		String[] bandName=reader.getBands();
		assertNotNull(bandName);
	}

	/**
	 * Run the String getDisplayName(int) method test
	 */
	public void testGetDisplayName() {
		String bandName=reader.getBandName(0);
		assertNotNull(bandName);
	}

	/**
	 * Run the String getFormat() method test
	 */
	public void testGetFormat() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getFormat() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the H5File getH5file() method test
	 */
	public void testGetH5file() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getH5file() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the int getHeight() method test
	 */
	public void testGetHeight() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getHeight() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the String getImgName() method test
	 */
	public void testGetImgName() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getImgName() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the String getInternalImage() method test
	 */
	public void testGetInternalImage() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getInternalImage() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the int getNBand() method test
	 */
	public void testGetNBand() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getNBand() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the int getNumberOfBytes() method test
	 */
	public void testGetNumberOfBytes() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getNumberOfBytes() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the File getOverviewFile() method test
	 */
	public void testGetOverviewFile() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the getOverviewFile() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the double getPRF(int, int) method test
	 */
	public void testGetPRF() {
		fail("Newly generated method - fix or disable");
		// add test code here
		int x = 0;
		int y = 0;
		// This class does not have a public, no argument constructor,
		// so the getPRF() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the boolean initialise() method test
	 */
	public void testInitialise() {
		fail("Newly generated method - fix or disable");
		// add test code here
		// This class does not have a public, no argument constructor,
		// so the initialise() method can not be tested
		assertTrue(false);
	}

	/**
	 * Run the void preloadLineTile(int, int, int) method test
	 */
	public void testPreloadLineTile() {
		fail("Newly generated method - fix or disable");
		// add test code here
		int y = 0;
		int height = 0;
		int band = 0;
		// This class does not have a public, no argument constructor,
		// so the preloadLineTile() method can not be tested
		assertTrue(false);
	}
}

/*$CPS$ This comment was generated by CodePro. Do not edit it.
 * patternId = com.instantiations.assist.eclipse.pattern.testCasePattern
 * strategyId = com.instantiations.assist.eclipse.pattern.testCasePattern.junitTestCase
 * additionalTestNames = 
 * assertTrue = false
 * callTestMethod = true
 * createMain = true
 * createSetUp = true
 * createTearDown = true
 * createTestFixture = false
 * createTestStubs = false
 * methods = getBandName(I),getBands(),getDisplayName(I),getFormat(),getH5file(),getHeight(),getImgName(),getInternalImage(),getNBand(),getNumberOfBytes(),getOverviewFile(),getPRF(I!I),initialise(),preloadLineTile(I!I!I)
 * package = org.geoimage.impl.cosmo
 * package.sourceFolder = Sumo/GeoImageViewer/src/test
 * superclassType = junit.framework.TestCase
 * testCase = CosmoSkymedImageTest
 * testClassType = org.geoimage.impl.cosmo.CosmoSkymedImage
 */