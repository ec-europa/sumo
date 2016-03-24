/*
 * 
 */
package others.h5;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;


public class ReadTestFileH5 {
	private static final String filepath = "C:\\tmp\\sumo_images\\CSKS4_SCS_B_HI_24_HH_RD_SF_20130306171306_20130306171313\\CSKS4_SCS_B_HI_24_HH_RD_SF_20130306171306_20130306171313.h5";


	public void read() throws Exception{
		H5File h5file=new H5File(filepath);


		H5ScalarDS imagedata = (H5ScalarDS) h5file.get("S01/SBI");
		imagedata.open();
/*
		if (imagedata == null) {
	        imagedata = (H5ScalarDS) h5file.get("MBI");
	    }
	    List metadata = imagedata.getMetadata();
	    metadata.addAll(h5file.get("/").getMetadata());
	    metadata.addAll(h5file.get("S01").getMetadata());
*/
	    imagedata.close(0);


	}





	public static void main (String args[]){

		try {
			new ReadTestFileH5().read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}
