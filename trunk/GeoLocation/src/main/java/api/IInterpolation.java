package api;

import interpolation.HermiteInterpolation;

import java.util.List;

import metadata.S1Metadata;

public interface IInterpolation {
	public HermiteInterpolation.InterpolationResult interpolation(double[] subTimesDiffRef, 
						List<S1Metadata.OrbitStatePosVelox> vpList,
						double[] timeStampInitSecondsRefPointsInterp,
						int idxInitTime, int idxEndTime,
						double deltaT);
}
