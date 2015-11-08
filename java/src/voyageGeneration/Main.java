package voyageGeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private static HashMap<Integer, ArrayList<Installation>> installationSetsByFrequency;
	private static ArrayList<Voyage> voyageSet;
	private static HashMap<Vessel, List<Voyage>> voyageSetByVessel;
	private static HashMap<Vessel, HashMap<Installation, List<Voyage>>> voyageSetByVesselAndInstallation;
	private static HashMap<Vessel, HashMap<Integer, List<Voyage>>> voyageSetByVesselAndDuration;
	private static IO io;
	private static long startTime, stopTime;
	private static String inputFileName = "data/input/Input data.xls",
			outputFileName = "data/output/"; //sets the folder see the constructor of IO for the filename format
	
	
	public static void main(String[] args) {
		startTime = System.nanoTime();
		voyageSet = new ArrayList<Voyage>();
		voyageSetByVessel = new HashMap<Vessel, List<Voyage>>(); //initialize the voyage set, R_v in the mathematical model
		voyageSetByVesselAndInstallation = new HashMap<Vessel, HashMap<Installation, List<Voyage>>>(); //initialize the voyage set indexed by both vessel and installation, R_vi in the mathematical model
		voyageSetByVesselAndDuration = new HashMap<Vessel, HashMap<Integer, List<Voyage>>>(); //initialize the voyage set indexed by both vessel and duration, R_vl in the mathematical model
		installationSetsByFrequency = new HashMap<Integer, ArrayList<Installation>>(); //initialize the set of installation for each frequency, Nf
		getData();	//get the input data of installations, vessels and distances from the excel input file 
		generateVesselSets(); //generate a set of vessels for each sailing speed, sorted descending by capacity
		generateInstallationSubsets();	//generate all possible installation subsets with minimum and mamimum number of installations
		
		//need cost, duration and installations visited
		for (Vessel[] vesselSet : vesselSets) {
			generateVoyageSet(vesselSet); //generates the voyages for the vessel set and adds them to voyageSet 
		}
		generateVoyages();
		generateVoyageSetByVesselAndInstallation();
		generateVoyageSetByVesselAndDuration();
		generateInstallationSetsByFrequency();
		
		stopTime = System.nanoTime();
		io.writeOutputToDataFile(installations, vessels, voyageSet, voyageSetByVessel, voyageSetByVesselAndInstallation, voyageSetByVesselAndDuration, installationSetsByFrequency, stopTime-startTime); //stopTime-startTime equals the execution time of the program
		//io.writeOutputToTextFile(voyageSet, voyageSetByVesselAndInstallation, voyageSetByVesselAndDuration, stopTime-startTime); //stopTime-startTime equals the execution time of the program
	}
	
	private static void generateVoyages() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				if (! voyageSet.contains(voyage)) {
					voyageSet.add(voyage);
				}
			}
		}
		Collections.sort(voyageSet);
	}

	private static void generateVoyageSetByVesselAndDuration() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			List<Voyage> voyages = voyageSetByVessel.get(vessel);//all voyages for a vessel 
			HashMap<Integer, List<Voyage>> voyageSetByDuration = new HashMap<Integer, List<Voyage>>();
			for (Voyage voyage : voyages) { //loop through all voyages for the vessel
				Integer tempDuration = (int) (voyage.getDepartureTime() - 8) / 24; //have to cast from double to int 
				List<Voyage> tempVoyageList = voyageSetByDuration.get(tempDuration); //get the current list of voyages for the combination of duration and vessel
				if (tempVoyageList == null) {//if no list of voyages has been instantiated
					tempVoyageList = new ArrayList<Voyage>();
				}
				tempVoyageList.add(voyage); //add the new voyage to the list of voyages for that combination of installation and vessel
				voyageSetByDuration.put(tempDuration, tempVoyageList); //change the list of voyages for the combination of installation and vessel
				
			}
			voyageSetByVesselAndDuration.put(vessel, voyageSetByDuration);
			List<Integer> durations = new ArrayList<>();// the list of feasible durations, the rest of the method adds an empty arraylist for the durations with no voyages
			for (int i = io.getMinDuration(); i < io.getMaxDuration(); i+=24) {
				durations.add((i-8)/24);
			}
			for (Integer d : durations) {
				if (voyageSetByDuration.get(d) == null) {
					voyageSetByDuration.put(d, new ArrayList<Voyage>());
				}
			}
		}
	}
	
	private static void generateVoyageSetByVesselAndInstallation() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			List<Voyage> voyages = voyageSetByVessel.get(vessel);//all voyages for a vessel 
			HashMap<Installation, List<Voyage>> voyageSetByInstallation = new HashMap<Installation, List<Voyage>>();
			for (Voyage voyage : voyages) { //loop through all voyages for the vessel
				for (int installationNumber : voyage.getVisited()) { //loop through all installations visited in the voyage
					Installation tempInst = installations[installationNumber];
					List<Voyage> tempVoyageList = voyageSetByInstallation.get(tempInst); //get the current list of voyages for the combination of installation and vessel
					if (tempVoyageList == null) {//if no list of voyages has been instantiated
						tempVoyageList = new ArrayList<Voyage>();
					}
					tempVoyageList.add(voyage); //add the new voyage to the list of voyages for that combination of installation and vessel
					voyageSetByInstallation.put(tempInst, tempVoyageList); //change the list of voyages for the combination of installation and vessel
				}
				voyageSetByVesselAndInstallation.put(vessel, voyageSetByInstallation);
			}
			for (int i = 0; i < installations.length; i++) { // a for loop to add empty arraylists for the installations not visited by the vessel
				if (voyageSetByInstallation.get(installations[i]) == null){
					voyageSetByInstallation.put(installations[i], new ArrayList<Voyage>());
				}
			}
		}
	}


	private static void generateVoyageSet(Vessel[] vesselSet) {
		Vessel vesselMax = vesselSet[0]; //the vessel sets are sorted descending by capacity, i.e. the first vessel has the largest capacity 
		ArrayList<Installation[]> possibleInstallationSubsets =  getPossibleInstallationSubsets(vesselMax.getCapacity());
		List<Voyage> cheapestVoyages = new ArrayList<Voyage>();
		for (Installation[] installationSubset : possibleInstallationSubsets) {
			Generator generator = new Generator(installationSubset, vesselMax, distances, io.getMinDuration(), io.getMaxDuration());
			Voyage cheapestVoyage = generator.findCheapestVoyage();
			if (cheapestVoyage != null) {
				cheapestVoyages.add(cheapestVoyage);
			}
		}
		voyageSetByVessel.put(vesselMax, cheapestVoyages); //add the voyageSet of voyageMax
		for (int i=1;i<vesselSet.length;i++) {//loop through the rest of the vessels and add the voyages from vesselMax if the capacity is not to high
			List<Voyage> tempCheapestVoyages = new ArrayList<Voyage>();
			Vessel vessel = vesselSet[i];
			for (Voyage voyage : cheapestVoyages) {
				if (voyage.getCapacityUsed() <= vessel.getCapacity()) {
					tempCheapestVoyages.add(voyage);
				}
			}
			voyageSetByVessel.put(vessel, tempCheapestVoyages);//add the voyages of the vessel to voyageSet
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
		installationSubsets = sb.generateSubsets(installations, io.getMinNumberOfInstallations(), io.getMaxNumberOfInstallations());
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
	
	private static void generateInstallationSetsByFrequency() {
		int minFrequency = Integer.MAX_VALUE; //any frequency will be lower than this
		int maxFrequency = Integer.MIN_VALUE; //any frequency will be higher than this
		for (int i = 1; i < installations.length; i++) { //starts at 1 to ignore the frequency of the depot
			Installation installation = installations[i];
			int frequency = installation.getFrequency();
			if (frequency < minFrequency) {
				minFrequency = frequency;
			}
			if (frequency > maxFrequency) {
				maxFrequency = frequency;
			}
		}
		for (int f = minFrequency; f <= maxFrequency; f++) {
			ArrayList<Installation> installationList = new ArrayList<>();
			for (int i = 1; i < installations.length; i++) {//starts at 1 to ignore the frequency of the depot
				if (installations[i].getFrequency() == f) {
					installationList.add(installations[i]);
				}
			}
			installationSetsByFrequency.put(f, installationList);
		}
	}
	
	private static void getData() {
		io = new IO(inputFileName, outputFileName);
		installations = io.getInstallations();
		vessels = io.getVessels();
		distances = io.getDistances();
	}
}