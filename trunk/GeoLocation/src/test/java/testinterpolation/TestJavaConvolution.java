package testinterpolation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathArrays;

public class TestJavaConvolution {

	public static double[] convolution(double a[],double b[]){
		return MathArrays.convolve(a,b);
	}
	
	/**
	 * Convolution U(m)=Sum( H(m)X(n-m) )    0<=m=<(size(H)-1) n=size(H)
	 * 
	 * This function reproduce the matlab conv function with shape='valid'
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] linearConvolutionMatlabValid(double a[],double b[]){
		//size of the array result without zero padd values
		int sizeResult=a.length+b.length-1;
		int matlabSizeResult=a.length-b.length+1;
		
		b=ArrayUtils.add(b,0);
		List<Integer> idxPadded=new ArrayList<Integer>();
		
		double[] u=new double[sizeResult];
		int idU=0;
		for(int n=0;n<sizeResult;n++){
			double val=0;
			
			
			for(int m=0;m<=n;m++){
				
				int idx1=m;
				int idx2=n-m;
				
				if(idx2>=0&&idx1>=0&&idx2<b.length-1&&idx1<a.length){
					val=val+a[idx1]*b[idx2];
				}	
			}

			u[idU]=val;
			idU++;
		}
		int diff=(sizeResult-matlabSizeResult);
		int idxStart=diff/2+diff%2;
		int idxEnd=u.length-diff/2;
		u=ArrayUtils.subarray(u, idxStart, idxEnd);
		
		
		return u; 
	}
	
	
	
	
	public static void main(String[] args){
		double []a={1,2,3};
		double []b={1,2};
		//double[] r=TestJavaConvolution.convolution(a,b);
		double[] r=TestJavaConvolution.linearConvolutionMatlabValid(a,b);
		
		
		for(int i=0;i<r.length;i++){
			System.out.println("  "+r[i]+"  ");
		}
	}
	
	
}
