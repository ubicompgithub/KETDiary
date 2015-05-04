package ubicomp.ketdiary.data.structure;

public class BreathDetail {

	public TimeValue getTv() {
		return tv;
	}

	public int getBlowStartTimes() {
		return blowStartTimes;
	}

	public int getBlowBreakTimes() {
		return blowBreakTimes;
	}

	public float getPressureDiffMax() {
		return pressureDiffMax;
	}

	public float getPressureMin() {
		return pressureMin;
	}

	public float getPressureAverage() {
		return pressureAverage;
	}

	public int getVoltageInit() {
		return voltageInit;
	}

	public long getDisconnectionMillis() {
		return disconnectionMillis;
	}

	public int getSerialDiffMax() {
		return serialDiffMax;
	}

	public float getSerialDiffAverage() {
		return serialDiffAverage;
	}

	private TimeValue tv;
	private int blowStartTimes;
	private int blowBreakTimes;
	private float pressureDiffMax;
	private float pressureMin;
	private float pressureAverage;
	private int voltageInit;
	private long disconnectionMillis;
	private int serialDiffMax;
	private float serialDiffAverage;
	private String sensorId;

	public String getSensorId() {
		return sensorId;
	}

	public BreathDetail(long timestamp,
			int blow_start_times,
			int blow_break_times,
			float pressure_diff_max,
			float pressure_min,
			float pressure_average,
			int voltage_init,
			long disconnection_millis,
			int serial_diff_max,
			float serial_diff_average,
			String sensorId) {

		this.tv = TimeValue.generate(timestamp);
		this.blowStartTimes = blow_start_times;
		this.blowBreakTimes = blow_break_times;
		this.pressureDiffMax = pressure_diff_max;
		this.pressureMin = pressure_min;
		this.pressureAverage = pressure_average;
		this.voltageInit = voltage_init;
		this.disconnectionMillis = disconnection_millis;
		this.serialDiffAverage = serial_diff_average;
		this.serialDiffMax = serial_diff_max;
		this.sensorId = sensorId;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(tv.toString());
		sb.append(' ');
		sb.append("bst=");
		sb.append(blowStartTimes);
		sb.append(' ');
		sb.append("bbt=");
		sb.append(blowBreakTimes);
		sb.append(' ');
		sb.append("pdm=");
		sb.append(pressureDiffMax);
		sb.append(' ');
		sb.append("pa=");
		sb.append(pressureAverage);
		sb.append(' ');
		sb.append("vi=");
		sb.append(voltageInit);
		sb.append(' ');
		sb.append("dm=");
		sb.append(disconnectionMillis);
		sb.append(' ');
		sb.append("sda=");
		sb.append(serialDiffAverage);
		sb.append(' ');
		sb.append("sdm=");
		sb.append(serialDiffMax);
		sb.append(' ');
		sb.append("sensorId=");
		sb.append(sensorId);
		return sb.toString();
	}

}
