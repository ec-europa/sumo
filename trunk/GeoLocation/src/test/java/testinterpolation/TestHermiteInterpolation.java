package testinterpolation;

import interpolation.HermiteInterpolation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHermiteInterpolation {

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
	public void testInterpolation() {
		
	}

	@Test
	public void testHermiteMatrix() {
		HermiteInterpolation hi=new HermiteInterpolation();
		double[][] result=hi.hermiteMatrix(4);
		
		for (double[] row:result){
			for(int i=0;i<row.length;i++){
				System.out.print(row[i]+"  ");
				
			}
			System.out.println();
		}
	}
	
	@Test
	public void testInvertMatrix() {
		HermiteInterpolation hi=new HermiteInterpolation();
		double[][] result=hi.hermiteMatrix(4);
		result=hi.invertMatrix(result);
		
		for (double[] row:result){
			for(int i=0;i<row.length;i++){
				System.out.print(row[i]+"  ");
				
			}
			System.out.println();
		}
		
		
		
		
	}

}
