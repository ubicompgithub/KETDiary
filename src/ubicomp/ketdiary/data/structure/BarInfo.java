package ubicomp.ketdiary.data.structure;

public class BarInfo{
	private float emotion = 0.F;
	private float craving=0.F;
	private float brac=0.F;
	private int week;
	private boolean hasData;
	private boolean drink;
	private TimeValue tv;
	
	public BarInfo (float emotion, float craving, float brac, int week,boolean hasData,TimeValue tv, boolean drink){
		this.emotion=emotion>0.f?emotion:0;
		this.craving=craving >0.f?craving:0;
		this.brac = drink?brac:0;
		this.week = week;
		this.hasData = hasData;
		this.tv = tv;
		this.drink = drink;
	}

	public float getEmotion() {
		return emotion;
	}

	public float getCraving() {
		return craving;
	}

	public float getBrac() {
		return brac;
	}

	public int getWeek() {
		return week;
	}

	public boolean isHasData() {
		return hasData;
	}

	public boolean isDrink() {
		return drink;
	}

	public TimeValue getTv() {
		return tv;
	}

}