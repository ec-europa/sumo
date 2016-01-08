package safe.reader;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAnnotationReader {
	private final String xml="C://tmp//sumo_images//S1Simulated//RS2Gibraltar//IW//RS2_IW_SLC__1SDH_20130425T182337_20130425T182422_001771_000001_F3DD.SAFE//annotation//rs2-iw1-slc-hh-20130425t182337-20130425t182420-001771-000001-001.xml";


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
	
	@Test
	public void testgetGridPoints() throws JAXBException{
		SumoAnnotationReader reader =new SumoAnnotationReader(xml);
		reader.getGridPoints();
		
		assertNotNull(reader);
		
		
	}

}
