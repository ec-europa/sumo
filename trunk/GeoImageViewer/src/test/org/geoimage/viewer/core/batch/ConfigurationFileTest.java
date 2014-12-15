package org.geoimage.viewer.core.batch;

import static org.junit.Assert.*;

import java.io.IOException;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationFileTest {
	private static ConfigurationFile cf=null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			cf=new ConfigurationFile("C:\\tmp\\output\\analysis.conf");
			//assertTrue(true);
		} catch (IOException e) {
			fail(e.getMessage());
		}
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
	public void testGetProperty() {
		cf.getProperty(ConfigurationFile.BUFFER_PARAM);
	}

	@Test
	public void testGetThresholdArray() {
		cf.getThresholdArray();
	}

	@Test
	public void testGetBuffer() {
		cf.getBuffer();
	}

	@Test
	public void testGetShapeFile() {
		cf.getShapeFile();
	}

	@Test
	public void testGetOutputFolder() {
		cf.getOutputFolder();
	}

	@Test
	public void testGetInputFolder() {
		cf.getInputFolder();
	}

	@Test
	public void testGetInputImage() {
		cf.getInputImage();
	}

	@Test
	public void testUseLocalConfigurationFiles() {
		cf.useLocalConfigurationFiles();
	}

	@Test
	public void testMain() {

	}

}
