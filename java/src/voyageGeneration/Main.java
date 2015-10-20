package voyageGeneration;

public class Main {
	
	//husk load factor
	//husk at depot er forste node i installationsubset som sendes til generator
	
	//each row consists of name, openingHour, closingHour, demand, frequency, serviceTime
	private static String[][] installationsData = {
			{"FMO","8","16","0","11","8"},
			{"1","0","24","2","1","8"},
			{"2","0","24","4","1","8"},
			};
	
	// Each row consists of name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumtionDepot, fuelConsumptionInstallation
	private static String[][] vesselsData = {
		{"FarSeeker", "10", "1", "1", "2", "0", "1"}
		};
	
	private static Installation[] installations;
	private static Vessel[] vessels;
	private static int maxDuration = 500;
	
	public static void main(String[] args) {
		generateInstallations();
		generateVessels();
		//test
		Generator generator = new Generator(installations, vessels[0], maxDuration);
		Label cheapestVoyage = generator.findCheapestVoyage();
		System.out.println("Sequence of installations visited");
		for (int i = 0; i < cheapestVoyage.getVisited().length; i++) {
			System.out.println(cheapestVoyage.getVisited()[i]);
		}
		System.out.println("Cost of cheapest voyage");
		System.out.println(cheapestVoyage.getCost());
	}
	
	private static void generateInstallations() {
		installations = new Installation[installationsData.length];
		for (int i=0;i<installationsData.length;i++) {
			String[] data = installationsData[i];
			String name = data[0];
			int openingHour = Integer.parseInt(data[1]);
			int closingHour = Integer.parseInt(data[2]);
			int demand = Integer.parseInt(data[3]);
			int frequency = Integer.parseInt(data[4]);
			int serviceTime = Integer.parseInt(data[5]);
					
			installations[i] = new Installation(name, openingHour, closingHour, demand, frequency, serviceTime, i);
		}
	}

	private static void generateVessels () {
		vessels = new Vessel[vesselsData.length];
		for (int i=0;i<vesselsData.length;i++) {
			String[] data = vesselsData[i];
			String name = data[0];
			int capacity = Integer.parseInt(data[1]);
			int speed = Integer.parseInt(data[2]);
			int unitFuelCost  = Integer.parseInt(data[3]);
			double fuelConsumptionSailing = Double.parseDouble(data[4]);
			double fuelConsumptionDepot = Double.parseDouble(data[5]);
			double fuelConsumptionInstallation = Double.parseDouble(data[6]);
					
			vessels[i] = new Vessel(name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumptionDepot, fuelConsumptionInstallation);
		}
	}
}
