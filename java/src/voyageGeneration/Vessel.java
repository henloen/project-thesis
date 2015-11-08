package voyageGeneration;

public class Vessel implements Comparable<Vessel>{
	private String name;
	private int capacity, speed, unitFuelCost, timeCharterCost, numberOfDaysAvailable;
	private double fuelConsumptionSailing, fuelConsumptionDepot, fuelConsumptionInstallation;
	
	

	public Vessel(String name, int capacity, int speed, int unitFuelCost,
			double fuelConsumptionSailing, double fuelConsumptionDepot,
			double fuelConsumptionInstallation, int timeCharterCost, int numberOfDaysAvailable) {
		this.name = name;
		this.capacity = capacity;
		this.speed = speed;
		this.unitFuelCost = unitFuelCost;
		this.fuelConsumptionSailing = fuelConsumptionSailing;
		this.fuelConsumptionDepot = fuelConsumptionDepot;
		this.fuelConsumptionInstallation = fuelConsumptionInstallation;
		this.timeCharterCost = timeCharterCost;
		this.numberOfDaysAvailable = numberOfDaysAvailable;
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public double getFuelCostSailing() {
		return fuelConsumptionSailing*unitFuelCost;
	}
	
	public double getFuelCostDepot() {
		return fuelConsumptionDepot*unitFuelCost;
	}	

	public double getFuelCostInstallation() {
		return fuelConsumptionInstallation*unitFuelCost;
	}
	
	
	public int getTimeCharterCost() {
		return timeCharterCost;
	}

	public int getNumberOfDaysAvailable() {
		return numberOfDaysAvailable;
	}

	public int compareTo(Vessel otherVessel) {
		return (otherVessel.capacity - capacity);
	}

}
