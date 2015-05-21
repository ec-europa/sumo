package api;

import java.util.List;

import jrc.it.geolocation.interpolation.HermiteInterpolation;
import jrc.it.geolocation.metadata.S1Metadata;

public interface IInterpolation {
	public HermiteInterpolation.InterpolationResult interpolation(double[] subTimesDiffRef, 
						List<S1Metadata.OrbitStatePosVelox> vpList,
						double[] timeStampInitSecondsRefPointsInterp,
						int idxInitTime, int idxEndTime,
						double deltaT);
}
