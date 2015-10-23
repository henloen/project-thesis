package voyageGeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
	private static ArrayList<Installation[]> installationSubsets;
	private static HashMap<Vessel, List<Label>> voyageSet;
	private static IO io;
	private static long startTime, stopTime;
	private static double loadFactor = 1.2;
	private static int minDuration = 56, //from Halvorsen-Weare: 2 days = 56 hours
			maxDuration = 80,//from Halvorsen-Weare: 3 days = 80 hours
			numberOfNodes = 15, //problem size, includes the depot
			numberOfInstallationAttributes = 6,
			numberOfVessels = 5,
			numberOfVesselAttributes = 7,
			minNumberOfInstallations = 1,//set to 1 because Halvorsen-Weare let's the minimum duration limit the minimum number of installations (H-W also does this)
			maxNumberOfInstallations = 8;//from Halvorsen-Weare,
	private static String inputFileName = "data/input/Input data.xls",
			outputFileName = "data/output/"; //sets the folder see the constructor of IO for the filename format
	
	
	public static void main(String[] args) {
		startTime = System.nanoTime();
		voyageSet = new HashMap<Vessel, List<Label>>(); //initialize the voyage set, R_v in the mathematical model
		getData();	//get the input data of installations, vessels and distances from the excel input file 
		generateVesselSets(); //generate a set of vessels for each sailing speed, sorted descending by capacity
		generateInstallationSubsets();	//generate all possible installation subsets with minimum and mamimum number of installations
		
		//need cost, duration and installations visited
		for (Vessel[] vesselSet : vesselSets) {
			generateVoyageSet(vesselSet); //generates the voyages for the vessel set and adds them to voyageSet 
		}
		stopTime = System.nanoTime();
		io.writeSolutionToFile(voyageSet,stopTime-startTime,minDuration,maxDuration,minNumberOfInstallations,maxNumberOfInstallations); //stopTime-startTime equals the execution time of the program
	}
	
	private static void generateVoyageSet(Vessel[] vesselSet) {
		Vessel vesselMax = vesselSet[0]; //the vessel sets are sorted descending by capacity, i.e. the first vessel has the largest capacity 
		ArrayList<Installation[]> possibleInstallationSubsets =  getPossibleInstallationSubsets(vesselMax.getCapacity());
		List<Label> cheapestVoyages = new ArrayList<Label>();
		for (Installation[] installationSubset : possibleInstallationSubsets) {
			Generator generator = new Generator(installationSubset, vesselMax, distances, minDuration, maxDuration);
			Label cheapestVoyage = generator.findCheapestVoyage();
			if (cheapestVoyage != null) {
				cheapestVoyages.add(cheapestVoyage);
			}
		}
		voyageSet.put(vesselMax, cheapestVoyages); //add the voyageSet of voyageMax
		for (int i=1;i<vesselSet.length;i++) {//loop through the rest of the vessels and add the voyages from vesselMax if the capacity is not to high
			List<Label> tempCheapestVoyages = new ArrayList<Label>();
			Vessel vessel = vesselSet[i];
			for (Label voyage : cheapestVoyages) {
				if (voyage.getCapacityUsed() <= vessel.getCapacity()) {
					tempCheapestVoyages.add(voyage);
				}
			}
			voyageSet.put(vessel, tempCheapestVoyages);//add the voyages of the vessel to voyageSet
		}
	}
	
	private static ArrayList<Installation[]> getPossibleInstallationSubsets(int maxCapacity) {
		ArrayList<Installation[]> possibleInstallationSubsets = new ArrayList<Installation[]>();
		for (Installation[] installationSubset : installationSubsets) {
			if (capacityNeeded(installationSubset) <= maxCapacity) {
				possibleInstallationSubsets.add(installationSubset);
			}
		}
		return possibleInstallationSubsets;
	}
	
	private static int capacityNeeded(Installation[] installationSubset) {
		int capacityNeeded = 0;
		for (int i = 0;i<installationSubset.length;i++) {
			capacityNeeded += installationSubset[i].getDemandPerVisit();
		}
		return capacityNeeded;
	}
	
	
	private static void generateInstallationSubsets(){
		Subsets sb = new Subsets();
		installationSubsets = sb.generateSubsets(installations, minNumberOfInstallations, maxNumberOfInstallations);
	}
	
	//Generates the set of vessels with the same sailing speed
	private static void generateVesselSets() {
		vesselSets = new ArrayList<Vessel[]>();
		//first we count the amount of vessels for each sailing speed
		HashMap<Integer, Integer> sailingSpeeds = new HashMap<Integer, Integer>();
		for (int i=0; i<vessels.length; i++) {
			Integer sailingSpeed = Integer.valueOf(vessels[i].getSpeed());
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
			Arrays.sort(vesselSet);
			vesselSets.add(vesselSet);
		}
	}
	
	private static void getData() {
		io = new IO(numberOfNodes,numberOfInstallationAttributes,numberOfVessels,numberOfVesselAttributes, loadFactor, inputFileName, outputFileName);
		installations = io.getInstallations();
		vessels = io.getVessels();
		distances = io.getDistances();
	}
}