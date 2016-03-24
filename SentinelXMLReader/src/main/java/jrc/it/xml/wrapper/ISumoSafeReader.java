/*
 * 
 */
package jrc.it.xml.wrapper;

import org.jdom2.Namespace;

public interface ISumoSafeReader {
	public final Namespace xfdu = Namespace.getNamespace("xfdu",
			"urn:ccsds:schema:xfdu:1");
	public final Namespace s1sarl1 = Namespace.getNamespace("s1sarl1",
			"http://www.esa.int/safe/sentinel-1.0/sentinel-1/sar/level-1");
	public final Namespace safeNs = Namespace.getNamespace("safe",
			"http://www.esa.int/safe/sentinel-1.0");
	public final Namespace gml = Namespace.getNamespace("gml",
			"http://www.opengis.net/gml");
	public final Namespace s1 = Namespace.getNamespace("s1",
			"http://www.esa.int/safe/sentinel-1.0/sentinel-1");
	public final Namespace s1sar = Namespace.getNamespace("s1sar",
			"http://www.esa.int/safe/sentinel-1.0/sentinel-1/sar");
	public final Namespace s1sarl2 = Namespace.getNamespace("s1sarl1",
			"http://www.esa.int/safe/sentinel-1.0/sentinel-1/sar/level-2");
	public final Namespace gx = Namespace.getNamespace("gx",
			"http://www.google.com/kml/ext/2.2");
}
