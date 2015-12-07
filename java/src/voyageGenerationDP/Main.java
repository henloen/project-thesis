package voyageGenerationDP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
	
	private static ArrayList<Installation> installations;
	private static ArrayList<Vessel> vessels;
	private static double[][] distances;
	private static ArrayList<ArrayList<Vessel>> vesselSets;
	private static HashMap<Integer, ArrayList<Installation>> installationSetsByFrequency;
	private static ArrayList<Voyage> voyageSet;
	private static HashMap<Vessel, ArrayList<Voyage>> voyageSetByVessel;
	private static HashMap<Vessel, HashMap<Installation, ArrayList<Voyage>>> voyageSetByVesselAndInstallation;
	private static HashMap<Vessel, HashMap<Integer, ArrayList<Voyage>>> voyageSetByVesselAndDuration;
	private static IO io;
	private static long startTime, stopTime;
	private static String inputFileName = "data/input/Input data.xls",
			outputFileName = "data/output/"; //sets the folder, see the constructor of IO for the filename format
	//heuristic parameters
	private static int removeLongestArcs = 0, minInstallationsHeur = 0, maxArcsRemovedInstallation = 2;
	private static double capacityFraction = 0.6;
	
	public static void main(String[] args) {
		startTime = System.nanoTime();
		installationSetsByFrequency = new HashMap<Integer, ArrayList<Installation>>(); //initialize the set of installation for each frequency, Nf
		voyageSet = new ArrayList<Voyage>();
		voyageSetByVessel = new HashMap<Vessel, ArrayList<Voyage>>(); //initialize the voyage set, R_v in the mathematical model
		voyageSetByVesselAndInstallation = new HashMap<Vessel, HashMap<Installation, ArrayList<Voyage>>>(); //initialize the voyage set indexed by both vessel and installation, R_vi in the mathematical model
		voyageSetByVesselAndDuration = new HashMap<Vessel, HashMap<Integer, ArrayList<Voyage>>>(); //initialize the voyage set indexed by both vessel and duration, R_vl in the mathematical model
		
		getData();
		generateVesselSets();
		for (ArrayList<Vessel> vesselSet : vesselSets) {
			generateVoyageSetsByVessel(vesselSet);
		}
		generateVoyageSet();
		filterByHeuristics(); //filters voyageSetByVessel by reducing the number of voyages
		generateVoyageSetsByVesselAndInstallation();
		generateVoyageSetsByVesselAndDuration();
		generateInstallationSetsByFrequency();
		
		
		//printVoyages(); //helper function to see voyages
		
		stopTime = System.nanoTime();
		io.writeOutputToDataFile(installations, vessels, voyageSet, voyageSetByVessel, voyageSetByVesselAndInstallation, voyageSetByVesselAndDuration, installationSetsByFrequency, stopTime - startTime, removeLongestArcs, minInstallationsHeur, capacityFraction); //stopTime-startTime equals the execution time of the program
	}	
	
	//get data from input file
	private static void getData() {
		io = new IO(inputFileName, outputFileName);
		installations = io.getInstallations();
		vessels = io.getVessels();
		distances = io.getDistances();
	}
	
	//generate sets of vessels with equal properties - vessels that can use the same voyages
	private static void generateVesselSets() {
		Set<Vessel> allVessels = new HashSet<Vessel>(vessels); //make a set containing all vessels
		vesselSets = new ArrayList<ArrayList<Vessel>>(); //make the double arraylist containing the vesselSets
		while (! allVessels.isEmpty()) {
			ArrayList<Vessel> tempSet = new ArrayList<Vessel>();
			Vessel nextVessel = allVessels.iterator().next();
			tempSet.add(nextVessel);
			for (Vessel vessel : allVessels) {
				if ( (nextVessel != vessel)
						&& (nextVessel.getSpeed() == vessel.getSpeed())
						&& (nextVessel.getFuelCostDepot() == vessel.getFuelCostDepot())
						&& (nextVessel.getFuelCostInstallation() == vessel.getFuelCostInstallation())
						&& (nextVessel.getFuelCostSailing() == vessel.getFuelCostSailing())) {
					tempSet.add(vessel);
				}
			}
			allVessels.removeAll(tempSet);
			Collections.sort(tempSet);//sort by capacity, largest capacity first;
			vesselSets.add(tempSet);
		}
	}
	
	private static void generateVoyageSetsByVessel(ArrayList<Vessel> vesselSet) {
		Vessel maxVessel = vesselSet.get(0); //the first vessel in a vesselset has the highest capacity
		Generator generator = new Generator(installations, maxVessel, distances, io.getMinDuration(), io.getMaxDuration(), io.getMaxNumberOfInstallations());
		ArrayList<Voyage> cheapestVoyages = generator.findCheapestVoyages();
		voyageSetByVessel.put(maxVessel, cheapestVoyages);
		
		for (int i = 1; i < vesselSet.size(); i++) {
			Vessel tempVessel = vesselSet.get(i);
			ArrayList<Voyage> tempVoyages = new ArrayList<Voyage>();
			for (Voyage voy : cheapestVoyages) {
				if (voy.getCapacityUsed() <= tempVessel.getCapacity()) {
					tempVoyages.add(voy);
				}
			}
			voyageSetByVessel.put(tempVessel, tempVoyages);
		}
				
	}
	
	private static void generateVoyageSet() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				if (! voyageSet.contains(voyage)) {
					voyageSet.add(voyage);
				}
			}
		}
		Collections.sort(voyageSet);
	}
	
	private static void generateVoyageSetsByVesselAndInstallation() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			HashMap<Installation, ArrayList<Voyage>> voyageSetByInstallation = new HashMap<Installation, ArrayList<Voyage>>();
			for (Installation i : installations) {
				voyageSetByInstallation.put(i, new ArrayList<Voyage>());
			}
			List<Voyage> voyages = voyageSetByVessel.get(vessel);//all voyages for a vessel 
			for (Voyage voyage : voyages) { //loop through all voyages for the vessel
				for (int i = 0; i < (voyage.getVisited().size()-1); i++) { //loop through all installations visited in the voyage except the last one (depot)
					Installation tempInst = installations.get(voyage.getVisited().get(i));
					ArrayList<Voyage> tempVoyageList = voyageSetByInstallation.get(tempInst); //get the current list of voyages for the combination of installation and vessel
					tempVoyageList.add(voyage); //add the new voyage to the list of voyages for that combination of installation and vessel
					voyageSetByInstallation.put(tempInst, tempVoyageList); //change the list of voyages for the combination of installation and vessel
				}
			}
			voyageSetByVesselAndInstallation.put(vessel, voyageSetByInstallation);
		}
	}
	
	private static void generateVoyageSetsByVesselAndDuration() {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			HashMap<Integer, ArrayList<Voyage>> voyageSetByDuration = new HashMap<Integer, ArrayList<Voyage>>();
			for (int i = io.getMinDuration(); i <= io.getMaxDuration(); i+=24) { //instantiate arraylist for all durations from min to max duration
				voyageSetByDuration.put(((i-8)/24), new ArrayList<Voyage>());
			}
			
			ArrayList<Voyage> voyages = voyageSetByVessel.get(vessel);//all voyages for a vessel 
			for (Voyage voyage : voyages) { //loop through all voyages for the vessel
				Integer tempDuration = (int) (voyage.getDepartureTime() - 8) / 24; //have to cast from double to int 
				ArrayList<Voyage> tempVoyageList = voyageSetByDuration.get(tempDuration); //get the current list of voyages for the combination of duration and vessel
				tempVoyageList.add(voyage); //add the new voyage to the list of voyages for that combination of installation and vessel
				voyageSetByDuration.put(tempDuration, tempVoyageList); //change the list of voyages for the combination of installation and vessel
			}
			voyageSetByVesselAndDuration.put(vessel, voyageSetByDuration);
		}
	}
	
	private static void generateInstallationSetsByFrequency() {
		int minFrequency = Integer.MAX_VALUE; //any frequency will be lower than this
		int maxFrequency = Integer.MIN_VALUE; //any frequency will be higher than this
		for (int i = 1; i < installations.size(); i++) { //starts at 1 to ignore the frequency of the depot
			Installation installation = installations.get(i);
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
			for (int i = 1; i < installations.size(); i++) {//starts at 1 to ignore the frequency of the depot
				if (installations.get(i).getFrequency() == f) {
					installationList.add(installations.get(i));
				}
			}
			installationSetsByFrequency.put(f, installationList);
		}
	}
	
	private static void filterByHeuristics() {
		Heuristics heuristics = new Heuristics(distances, installations);
		if (removeLongestArcs > 0) {
			heuristics.removeLongestDistancePairs(removeLongestArcs, maxArcsRemovedInstallation, voyageSetByVessel);
		}
		if (minInstallationsHeur > 0) {
			heuristics.minInstallationsHeur(minInstallationsHeur, voyageSetByVessel);
		}
		if (capacityFraction > 0) {
			heuristics.minCapacityUsed(capacityFraction, voyageSetByVessel);
		}
	}
	
	private static void printVoyages() {
		ArrayList<Integer> voyageNumbers = new ArrayList<Integer>();
		voyageNumbers.add(1407);
		voyageNumbers.add(1507);
		voyageNumbers.add(1508);
		voyageNumbers.add(1508);
		voyageNumbers.add(827);
		voyageNumbers.add(861);
		voyageNumbers.add(1079);
		HashMap<Integer, Integer> installationNumbers = new HashMap<Integer, Integer>();
		for (Voyage voyage : voyageSet) {
			if (voyageNumbers.contains(voyage.getNumber())) {
				Integer installationNumber = voyage.getVisited().size() - 2;
				Integer currentValue = installationNumbers.get(installationNumber);
				if (currentValue == null ) {
					installationNumbers.put(installationNumber, 1);
				}
				else {
					installationNumbers.put(installationNumber, currentValue + 1);
				}
				System.out.println(voyage.getFullText());
			}
		}
		HashMap<Integer, Integer> installationNumbers2 = new HashMap<Integer, Integer>();
		for (Voyage voyage2 : voyageSet) {
			Integer installationNumber = voyage2.getVisited().size() - 2;
			Integer currentValue = installationNumbers2.get(installationNumber);
			if (currentValue == null ) {
				installationNumbers2.put(installationNumber, 1);
			}
			else {
				installationNumbers2.put(installationNumber, currentValue + 1);
			}
		}
		System.out.println(installationNumbers);
		System.out.println(installationNumbers2);
	}
	
	
}
