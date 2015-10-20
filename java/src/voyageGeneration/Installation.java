package voyageGeneration;

public class Installation {
	
	private String name;
	private int number, demand, frequency;
	private double openingHour, closingHour, serviceTime;
	
	public Installation(String name, double openingHour,
			double closingHour, int demand, int frequency, double serviceTime, int number) {
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

	public double getServiceTime() {
		return serviceTime;
	}

	public double getOpeningHour() {
		return openingHour;
	}


	public double getClosingHour() {
		return closingHour;
	}

	public int getNumber() {
		return number;
	}
	
	public double getTodaysOpeningHour(double time) {
		double day = Math.ceil(time/24);
		return  (day-1)*24 + openingHour;
	}
	
	public double getTodaysClosingHour(double time) {
		double day = Math.ceil(time/24.0);
		return (day-1)*24 + closingHour;
	}
	
	public String toString() {
		return ""+number;
	}

}
