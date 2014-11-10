package safe.reader;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Resource;
import javax.annotation.Resources;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class TestSafeReader {
	private String safeFile="C:\\tmp\\sumo_images\\S1Simulated\\RS2Gibraltar\\IW\\RS2_IW_SLC__1SDH_20130425T182337_20130425T182422_001771_000001_F3DD.SAFE\\manifest.safe";
	
	@BeforeClass
	public static void testSetup() {
	}

	@AfterClass
	public static void testCleanup() {
	    // Teardown for data used by the unit tests
	}
	
	@Test
	public void testSafeReader() {
		try {
			SafeReader safeReader=new SafeReader(safeFile);
			assertNotNull(safeReader);
		} catch (IllegalArgumentException | IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testGetMetadataObjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDataObjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMesasurementFiles() {
		try {
			SafeReader safeReader=new SafeReader(safeFile);
			Collection<String> files=safeReader.getMesasurementFiles();
			assertNotNull(files);
			
			for(String f:files){
				assertTrue(new File(f).exists());
			}
			
		} catch (IllegalArgumentException | IOException e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void testreadGeneralProductInformation() {
		try {
			SafeReader safeReader=new SafeReader(safeFile);
			safeReader.readGeneralProductInformation();
			assertTrue(true);
		} catch (IllegalArgumentException | IOException e) {
			fail(e.getMessage());
		}
	}
}
