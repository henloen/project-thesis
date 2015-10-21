package voyageGeneration;

import java.util.Arrays;

public class Main {
	
	//husk load factor
	
	/*
	 * How to set up the voyage generator:
	 * - Set maxDuration, the maximum duration of a voyage, measured in hours
	 * - Set numberOfInstallations, the number of installations to be read in from the excel input file
	 * - set numberOfInstallationAttributes, the number of attributes read for each installation (e.g. opening hours). Should normally be kept to 6
	 * - set numberOfVessels, the number of vessels to be read in from the excel input file
	 * - set numberOfVesselAttribtes, the number of attributes read for each vessel (e.g. capacity). Should normally be kept to 7
	 * - set fileName, the name of the input file
	 * - temporary: set the length of the installationSubset in Arrays.copyOfRange (third argument). Can't be larger than the length of installations
	 * 
	 */
	
	private static Installation[] installations;
	private static Vessel[] vessels;
	private static double[][] distances;
	private static int maxDuration = 500,
			numberOfInstallations = 15,
			numberOfInstallationAttributes = 6,
			numberOfVessels = 4,
			numberOfVesselAttributes = 7;
	private static String fileName = "src/voyageGeneration/Input data.xls";
	
	public static void main(String[] args) {
		getData();
		//adjust the third parameter of copyOfRange to set the number of installations used
		Installation[] installationSubset = Arrays.copyOfRange(installations,0,9);
		Vessel vessel = vessels[0];
		Generator generator = new Generator(installationSubset, vessel,distances, maxDuration);
		Label cheapestVoyage = generator.findCheapestVoyage();
		printSolution(cheapestVoyage);
	}
	
	private static void getData() {
		IO io = new IO(numberOfInstallations,numberOfInstallationAttributes,numberOfVessels,numberOfVesselAttributes, fileName);
		installations = io.getInstallations();
		vessels = io.getVessels();
		distances = io.getDistances();
	}
	
	private static void printSolution(Label solutionLabel) {
		System.out.println("------------------------------------------------------------------------------------------------------------------------");
		if (solutionLabel == null) {
			System.out.println("No feasible solution was found for the subproblem");
		}
		else {
			System.out.println("The solution to the subproblem is the label with these properties: ");
			System.out.println(solutionLabel.getFullText());
		}
	}
	
}
