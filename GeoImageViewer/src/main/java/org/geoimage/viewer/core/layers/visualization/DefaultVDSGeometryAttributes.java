package org.geoimage.viewer.core.layers.visualization;

import java.util.Date;

import org.geoimage.analysis.VDSSchema;

public class DefaultVDSGeometryAttributes extends AttributesGeometry {
	
	public DefaultVDSGeometryAttributes() {
		super(VDSSchema.schema);
		set(VDSSchema.ID,new Integer(0));
		set(VDSSchema.MAXIMUM_VALUE,new Integer(0));
		set(VDSSchema.TILE_AVERAGE,new Double(0));
		set(VDSSchema.TILE_STANDARD_DEVIATION,new Double(0));
		set(VDSSchema.THRESHOLD,new Double(0));
		set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS,new Integer(0));
		set(VDSSchema.RUN_ID,new Integer(0));
		set(VDSSchema.ESTIMATED_LENGTH,new Double(0));
		set(VDSSchema.ESTIMATED_WIDTH,new Double(0));
		set(VDSSchema.ESTIMATED_HEADING,new Double(0));
		set(VDSSchema.SIGNIFICANCE,new Double(0));
		set(VDSSchema.DATE,new Date());
		set(VDSSchema.VS,new Double(0));
		
	}

	
	
	
}
