/**
 * 
 */
package safe.reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import jrc.it.safe.reader.jaxb.AcquisitionPeriod;
import jrc.it.safe.reader.jaxb.FrameSet;
import jrc.it.safe.reader.jaxb.OrbitReference;
import jrc.it.safe.reader.jaxb.StandAloneProductInformation;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author argenpo
 *
 */
public class TestSumoJaxbSafeReader {
	private String safeFile="./manifest.safe";
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
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#SumoJaxbSafeReader(java.io.File)}.
	 */
	@Test
	public void testSumoJaxbSafeReaderFile() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			assertNotNull(reader);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}

	
	/**
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#getOrbitReference()}.
	 */
	@Test
	public void testGetOrbitReference() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			OrbitReference orbit=reader.getOrbitReference();
			assertNotNull(orbit);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#setOrbitReference(jrc.it.safe.reader.jaxb.OrbitReference)}.
	 */
	@Test
	public void testSetOrbitReference() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			AcquisitionPeriod period=reader.getAcquisitionPeriod();
			assertNotNull(period);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#getProductInformation()}.
	 */
	@Test
	public void testGetProductInformation() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			StandAloneProductInformation p=reader.getProductInformation();
			assertNotNull(p);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}


	/**
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#getAcquisitionPeriod()}.
	 */
	@Test
	public void testGetAcquisitionPeriod() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			AcquisitionPeriod acq=reader.getAcquisitionPeriod();
			assertNotNull(acq);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}


	/**
	 * Test method for {@link jrc.it.xml.wrapper.SumoJaxbSafeReader#getFrameSet()}.
	 */
	@Test
	public void testGetFrameSet() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			FrameSet frame=reader.getFrameSet();
			assertNotNull(frame);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}
	}

	
	/**
	 * 
	 */
	@Test
	public void testGetHrefsTiff() {
		try {
			SumoJaxbSafeReader reader=new SumoJaxbSafeReader(new File(safeFile));
			String[] tiffs=reader.getHrefsTiff();
			for(String t:tiffs)
				System.out.println("Tiff:"+t.toString());
			assertNotNull(tiffs);
		} catch (JDOMException | IOException | JAXBException e) {
			fail(e.getMessage());
		}

	}

		

}
