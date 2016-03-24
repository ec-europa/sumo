package others;

import java.net.URI;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.batch.MultipleBatchAnalysis;
import org.slf4j.LoggerFactory;

import jrc.it.geolocation.geo.ParallelGeoCoding;

public class TestLog4j2 {

	private static org.slf4j.Logger logger1=LoggerFactory.getLogger(MultipleBatchAnalysis.class);
	private static org.slf4j.Logger logger2=LoggerFactory.getLogger(GeoImageReader.class);
	private static org.slf4j.Logger logger3=LoggerFactory.getLogger(ParallelGeoCoding.class);
	private static org.apache.logging.log4j.Logger logger4=LogManager.getLogger("org.geoimage.viewer.core.batch");





	public static void main(String [] args){
		try{
			System.out.println(logger1.isDebugEnabled());
			System.out.println(logger1.isErrorEnabled());
			System.out.println(logger1.isInfoEnabled());

			System.out.println(logger2.isDebugEnabled());
			System.out.println(logger2.isErrorEnabled());
			System.out.println(logger2.isInfoEnabled());

			System.out.println(logger3.isDebugEnabled());
			System.out.println(logger3.isErrorEnabled());
			System.out.println(logger3.isInfoEnabled());

			System.out.println(logger4.isDebugEnabled());
			System.out.println(logger4.isErrorEnabled());
			System.out.println(logger4.isInfoEnabled());

			org.apache.logging.log4j.spi.LoggerContext context=LogManager.getContext();
			Logger logger = (Logger) LogManager.getLogger();
			LoggerConfig config=logger.get();
			Map<Property, Boolean> properties=config.getProperties();
			System.out.println(config.getName());
			LoggerContext ctx=LoggerContext.getContext();
			Object appenders=ctx.getConfiguration().getAppenders();
			System.out.println(appenders.toString());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.exit(0);
		}


	}
}
