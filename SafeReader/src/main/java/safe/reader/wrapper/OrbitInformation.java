package safe.reader.wrapper;

public class OrbitInformation {
	
		public static String ORBIT_TYPE_START="start";
		public static String ORBIT_TYPE_STOP="stop";
	
	
		private String orbitNumberStart;
		private String orbitNumberStop;
		private String relativeOrbitNumberStart;
		private String relativeOrbitNumberStop;
		private String cycleNumber;
		private String phaseIdentifier;
		
		
		public String getOrbitNumberStart() {
			return orbitNumberStart;
		}
		public void setOrbitNumberStart(String orbitNumberStart) {
			this.orbitNumberStart = orbitNumberStart;
		}
		public String getOrbitNumberStop() {
			return orbitNumberStop;
		}
		public void setOrbitNumberStop(String orbitNumberStop) {
			this.orbitNumberStop = orbitNumberStop;
		}
		public String getRelativeOrbitNumberStart() {
			return relativeOrbitNumberStart;
		}
		public void setRelativeOrbitNumberStart(String relativeOrbitNumberStart) {
			this.relativeOrbitNumberStart = relativeOrbitNumberStart;
		}
		public String getRelativeOrbitNumberStop() {
			return relativeOrbitNumberStop;
		}
		public void setRelativeOrbitNumberStop(String relativeOrbitNumberStop) {
			this.relativeOrbitNumberStop = relativeOrbitNumberStop;
		}
		public String getCycleNumber() {
			return cycleNumber;
		}
		public void setCycleNumber(String cycleNumber) {
			this.cycleNumber = cycleNumber;
		}
		public String getPhaseIdentifier() {
			return phaseIdentifier;
		}
		public void setPhaseIdentifier(String phaseIdentifier) {
			this.phaseIdentifier = phaseIdentifier;
		}
	
		
		
}
