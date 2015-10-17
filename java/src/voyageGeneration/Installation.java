package voyageGeneration;

public class Installation {
	
	private String name;
	private int serviceTime, openingHour, closingHour, number, demand, frequency;
	
	public double getDemandPerVisit(){
		return demand/frequency;
	}
	
	public String getName() {
		return name;
	}

	public int getDemand() {
		return demand;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getServiceTime() {
		return serviceTime;
	}

	public int getOpeningHour() {
		return openingHour;
	}


	public int getClosingHour() {
		return closingHour;
	}

	public int getNumber() {
		return number;
	}
	
	public int getTodaysOpeningHour(int time) {
		int day = (int) Math.ceil(time/24);
		return (day-1)*24 + openingHour;
	}
	
	public int getTodaysClosingHour(int time) {
		int day = (int) Math.ceil(time/24);
		return (day-1)*24 + closingHour;
	}

}
