package voyageGeneration;

import java.util.Arrays;

public class Main {
	
	//husk load factor
	//husk at depot er forste node i installationsubset som sendes til generator
	
	private static Installation[] installations;
	private static Vessel[] vessels;
	private static double[][] distances;
	private static int maxDuration = 500,
			numberOfInstallations = 15,
			numberOfInstallationAttributes = 6,
			numberOfVessels = 1,
			numberofVesselAttributes = 7;
	private static String fileName = "src/voyageGeneration/Input data.xls";
	
	public static void main(String[] args) {
		getData();
		//adjust the third parameter of copyOfRange to set the number of installations used
		Generator generator = new Generator(Arrays.copyOfRange(installations,0,9), vessels[0],distances, maxDuration);
		Label cheapestVoyage = generator.findCheapestVoyage();
		System.out.println("------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Cheapest voyage label: " + cheapestVoyage.getFullText());
	}
	
	private static void getData() {
		IO io = new IO(numberOfInstallations,numberOfInstallationAttributes,numberOfVessels,numberofVesselAttributes, fileName);
		installations = io.getInstallations();
		vessels = io.getVessels();
		distances = io.getDistances();
	}
	
	
}
