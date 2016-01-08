/**
 * 
 */
package org.geoimage.viewer.widget;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.imageio.ImageIO;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.impl.s1.Sentinel1SLC;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author argenpo
 *
 */
public class OverviewTest  {
	public static final String safe="C:/tmp/sumo_images/S1/IW/S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE/manifest.safe";
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.geoimage.viewer.widget.GeoOverviewToolbar#GeoOverviewToolbar()}.
	 */
	@Test
	public void testCreateOverviewFile() {
		try{
			ImageIO.scanForPlugins();
			Object o=ImageIO.getImageReadersByFormatName("tiff");
			List<GeoImageReader> readers= GeoImageReaderFactory.createReaderForName(safe,PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());
			Sentinel1SLC slc=(Sentinel1SLC)(readers.get(0));
			
			Overview overview=new Overview(null);
			overview.setGir(slc);
			overview.buildOverview();
			
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}	
		
	}

}
