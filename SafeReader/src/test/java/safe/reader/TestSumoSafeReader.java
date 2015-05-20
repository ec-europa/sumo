package safe.reader;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import safe.reader.wrapper.AcquisitionPeriod;

public class TestSumoSafeReader {
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
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
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
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
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
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
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
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
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
			SumoSafeReader safeReader=new SumoSafeReader(safeFile);
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
