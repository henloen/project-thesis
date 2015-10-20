package voyageGeneration;

public class Vessel {
	private String name;
	private int capacity, speed, unitFuelCost;
	private double fuelConsumptionSailing, fuelConsumptionDepot, fuelConsumptionInstallation;
	
	

	public Vessel(String name, int capacity, int speed, int unitFuelCost,
			double fuelConsumptionSailing, double fuelConsumptionDepot,
			double fuelConsumptionInstallation) {
		this.name = name;
		this.capacity = capacity;
		this.speed = speed;
		this.unitFuelCost = unitFuelCost;
		this.fuelConsumptionSailing = fuelConsumptionSailing;
		this.fuelConsumptionDepot = fuelConsumptionDepot;
		this.fuelConsumptionInstallation = fuelConsumptionInstallation;
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

}
