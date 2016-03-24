/*
 * 
 */
package others;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.geoimage.viewer.java2d.util.ScaleTransformation;
import org.jrc.sumo.util.Constant;


public class OverviewTest {
		public static void testOverview(){
			File f=new File("C:\\tmp\\sumo_images\\RS2_OK21011_PK229149_DK213789_F0W1_20110608_172531_HH_HV_SGF");
			//search for overview images
            for (File o : f.listFiles()) {
                if (o.isDirectory()) {
                    continue;
                }
                //BufferedImage bufferedImage=null;
                String simplefilename = o.getName().toLowerCase();
                if ((simplefilename.endsWith(".jpg")||simplefilename.endsWith(".tif")) && (simplefilename.contains("preview") || simplefilename.contains("browse"))) {
                    try {
                        BufferedImage temp=ScaleTransformation.scale(o,Constant.OVERVIEW_SIZE);
                        File overview = new File(".\\over.png");
                        ImageIO.write(temp, "png", overview);
                        //bufferedImage = ImageIO.read(overview);
                    } catch (IOException ex) {
                    	ex.printStackTrace();
                    }
                }
            }
		}
		
		
		public static void main(String[] args){
			OverviewTest.testOverview();
		}
}
