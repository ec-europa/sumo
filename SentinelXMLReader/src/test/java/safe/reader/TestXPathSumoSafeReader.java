package safe.reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import jrc.it.safe.reader.xpath.object.wrapper.AcquisitionPeriod;
import jrc.it.xml.wrapper.SumoXPathSafeReader;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestXPathSumoSafeReader {
	private String safeFile="C:\\tmp\\sumo_images\\S1Simulated\\RS2Gibraltar\\IW\\RS2_IW_SLC__1SDH_20130425T182337_20130425T182422_001771_000001_F3DD.SAFE\\manifest.safe";

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
	public void testSumoSafeReader() {
	}

	/*@Test
	public void testGetMesasurementFiles(){
		try {
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
			File[] files=safeReader.getMesasurementFiles();
			assertNotNull(files);
			
			for(File f:files){
				System.out.println(f.getPath());
				assertTrue(f.exists());
			}
			
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (JDOMException e) {
			fail(e.getMessage());
		}catch (JAXBException e) {
			fail(e.getMessage());
		}
	}*/
	@Test
	public void testGetMesasurementFilesFromSafe(){
		try {
			SumoXPathSafeReader safeReader=new SumoXPathSafeReader(safeFile);
			safeReader.readMesasurementFilesFromSafe();
			File[] files=safeReader.getMeasurements();
			assertNotNull(files);
			
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (JDOMException e) {
			fail(e.getMessage());
		}catch (JAXBException e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void readGeneralProductInformation(){
		try {
			SumoXPathSafeReader safeReader=new SumoXPathSafeReader(safeFile);
			safeReader.readGeneralProductInformation();
		
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (JDOMException e) {
			fail(e.getMessage());
		}catch (JAXBException e) {
			fail(e.getMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Test
	public void testReadGeneralProductInformation() {
		try {
			SumoXPathSafeReader safeReader=new SumoXPathSafeReader(safeFile);
			safeReader.readGeneralProductInformation();
		} catch (JDOMException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testReadAcqPeriod() {
		try {
			SumoXPathSafeReader safeReader=new SumoXPathSafeReader(safeFile);
			safeReader.readAcquisitionPeriod();
			AcquisitionPeriod acq=safeReader.getAcquPeriod();
			assertNotNull(acq);
		} catch (JDOMException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	@Test
	public void readorbitInformation() {
		try {
			SumoXPathSafeReader safeReader=new SumoXPathSafeReader(safeFile);
			safeReader.readAcquisitionPeriod();
			safeReader.readorbitInformation();
		} catch (JDOMException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	
	
	
}
