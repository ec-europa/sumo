import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;

public class TiffTest {
	private static final String filepath = "";

	public static void main(String args[]) {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("tiff");
			TIFFImageReader reader = null;
			while (readers.hasNext()) {
				Object r = readers.next();
				//if (r instanceof TIFFImageReader) {
				try {
					reader = (TIFFImageReader) r;

					ImageInputStream iis = ImageIO.createImageInputStream(filepath);
					reader.setInput(iis);

					//IIOMetadata meta = reader.getImageMetadata(0);
					TIFFDirectory td = TIFFDirectory.createFromMetadata(reader.getImageMetadata(0));
					// save the raster for further access during read(x,y)
					ImageReadParam param = new ImageReadParam();
					reader.readRaster(0, param);//(0).getData();



				//}
					System.out.println("OK:"+reader.getClass());
				} catch (Exception e) {
					System.out.println(r.getClass());
					e.printStackTrace();
				}

			}
	}
}
