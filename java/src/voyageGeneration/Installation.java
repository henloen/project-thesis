package voyageGeneration;

public class Installation {
	
	private String name;
	private int serviceTime, openingHour, closingHour, number, demand, frequency;
	
	public Installation(String name, int openingHour,
			int closingHour, int demand, int frequency, int serviceTime, int number) {
		this.name = name;
		this.serviceTime = serviceTime;
		this.openingHour = openingHour;
		this.closingHour = closingHour;
		this.number = number;
		this.demand = demand;
		this.frequency = frequency;
	}

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
//		System.out.println(Math.ceil((time/24.0)));
		int day = (int) Math.ceil(time/24.0);
//		System.out.println("Time: " + time);
//		System.out.println("Day: " + day);
		int res = (day-1)*24 + openingHour;
//		System.out.println("OpeningHourAbsolute: " + res);
		return res;
	}
	
	public int getTodaysClosingHour(int time) {
		int day = (int) Math.ceil(time/24.0);
		return (day-1)*24 + closingHour;

	}

}
