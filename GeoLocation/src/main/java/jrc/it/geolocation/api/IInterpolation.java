package jrc.it.geolocation.api;

import java.util.List;

import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.metadata.S1Metadata;

public interface IInterpolation {
	public void interpolation(double[] subTimesDiffRef,List<S1Metadata.OrbitStatePosVelox> vpList,
			double timeStampInitSecondsRefPointsInterp[],
			int idxInitTime, int idxEndTime,
			double deltaT)throws MathException;
}
