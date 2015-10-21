package voyageGeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
	private static ArrayList<Vessel[]> vesselSets;
	private static int maxDuration = 500,
			numberOfInstallations = 15,
			numberOfInstallationAttributes = 6,
			numberOfVessels = 5,
			numberOfVesselAttributes = 7;
	private static String fileName = "src/voyageGeneration/Input data.xls";
	
	
	public static void main(String[] args) {
		getData();	//get the input data of installations, vessels and distances from the excel input file 
		generateVesselSets(); //generate a set of vessels for each sailing speed
		Installation[] installationSubset = Arrays.copyOfRange(installations,0,9); //adjust the third parameter of copyOfRange to set the number of installations used
		Vessel vessel = vessels[0];
		Generator generator = new Generator(installationSubset, vessel,distances, maxDuration);
		Label cheapestVoyage = generator.findCheapestVoyage();
		printSolution(cheapestVoyage);
	}
	
	//Generates the set of vessels with the same sailing speed
	private static void generateVesselSets() {
		vesselSets = new ArrayList<Vessel[]>();
		//first we count the amount of vessels for each sailing speed
		HashMap<Integer, Integer> sailingSpeeds = new HashMap<Integer, Integer>();
		for (int i=0; i<vessels.length; i++) {
			Integer sailingSpeed = Integer.valueOf(vessels[i].getSpeed());
			System.out.println(sailingSpeed);
			if (! sailingSpeeds.containsKey(sailingSpeed)) {
				sailingSpeeds.put(sailingSpeed, 1); //add the sailing speed and set the count to 1
			}
			else {
				sailingSpeeds.put(sailingSpeed, sailingSpeeds.get(sailingSpeed)+1);//increase the count of that sailing speed by 1
			}
		}
		//then we loop through the different sailing speeds and make an array of vessels for each speed 
		for (Integer sailingSpeed : sailingSpeeds.keySet()) {
			Vessel[] vesselSet = new Vessel[sailingSpeeds.get(sailingSpeed)];
			int vesselSetIndex = 0;
			for (int i = 0; i<vessels.length;i++) {
				Vessel vessel = vessels[i];
				if (Integer.valueOf(vessel.getSpeed()).equals(sailingSpeed)) {
					vesselSet[vesselSetIndex] = vessel;
					vesselSetIndex++;
				}
			}
			vesselSets.add(vesselSet);
		}
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
