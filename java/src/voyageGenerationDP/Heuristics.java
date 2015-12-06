package voyageGenerationDP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Heuristics {
	
	private double[][] distances;
	private ArrayList<Installation> installations;
	
	public Heuristics(double[][] distances, ArrayList<Installation> installations) {
		this.distances = distances;
		this.installations = installations;
	}
	
	public void removeLongestDistancePairs(int numberOfPairs, int maxPerInstallation, HashMap<Vessel, ArrayList<Voyage>> voyageSetByVessel) {
		ArrayList<ArrayList<Integer>> pairs = longestArcs(numberOfPairs, maxPerInstallation);
		System.out.println(pairs);
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			ArrayList<Voyage> removeList = new ArrayList<Voyage>();
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				for (ArrayList<Integer> pair : pairs) {
					if (voyageContainsArc(voyage, pair)) {
						removeList.add(voyage);
					}
				}
			}
			ArrayList<Voyage> newVoyages = voyageSetByVessel.get(vessel);
			newVoyages.removeAll(removeList);
			voyageSetByVessel.put(vessel, newVoyages);
		}
		/*for (ArrayList<Integer> pair : pairs) {
			System.out.println(pair);
		}*/
	}
	
	private boolean voyageContainsArc(Voyage voyage, ArrayList<Integer> pair) {
		ArrayList<Integer> visitedList = voyage.getVisited();
		Integer firstInstallation = pair.get(0);
		Integer secondInstallation = pair.get(1);
		for (int i=0; i<visitedList.size()-1;i++) {
			if ((visitedList.get(i)==firstInstallation && visitedList.get(i+1)==secondInstallation)
					|| (visitedList.get(i)==secondInstallation && visitedList.get(i+1)==firstInstallation)) {
				return true;
			}
		}
		return false;
	}
	
	private void addArc(Double distance, ArrayList<Integer> arc, ArrayList<Double> longestDistances, HashMap<Double, ArrayList<Integer>> arcs, HashMap<Integer, Integer> installationCount) {
		longestDistances.add(distance);
		arcs.put(distance, arc);
		for (Integer i : arc) {
			installationCount.put(i, installationCount.get(i)+1);
		}
		Collections.sort(longestDistances);
	}
	
	private void removeArc(Double distance, ArrayList<Double> longestDistances, HashMap<Double, ArrayList<Integer>> arcs, HashMap<Integer, Integer> installationCount) {
		longestDistances.remove(distance);
		ArrayList<Integer> oldPair = arcs.remove(distance);
		for (Integer i : oldPair) {
			installationCount.put(i, installationCount.get(i)-1);
		}
	}
	
	private Double getDistance(Integer i, Integer j, HashMap<Double, ArrayList<Integer>> arcs) {
		for (Double distance : arcs.keySet()) {
			if (arcs.get(distance).contains(i) && arcs.get(distance).contains(j)) {
				return distance;
			}
		}
		return null;
	}
	
	private Double getShortestDistance(Integer i, HashMap<Double, ArrayList<Integer>> arcs) {
		ArrayList<Double> shortestList = new ArrayList<Double>();
		for (Double distance : arcs.keySet()) {
			if (arcs.get(distance).contains(i)) {
				shortestList.add(distance);
			}
		}
		Collections.sort(shortestList);
		if (shortestList.size() == 0) {
			return null;
		}
		else {
			return shortestList.get(0);
		}
	}
		
	
	
	private ArrayList<ArrayList<Integer>> longestArcs (int numberOfArcs, int maxPerInstallation) {
		HashMap<Double, ArrayList<Integer>> arcs = new HashMap<Double, ArrayList<Integer>>();
		HashMap<Integer, Integer> installationCount = new HashMap<Integer, Integer>();
		for (Installation installation : installations) {
			installationCount.put(installation.getNumber(), 0);
		}
		ArrayList<Double> longestDistances = new ArrayList<Double>();
		for (int i = 1; i < distances[0].length; i ++) {
			for (int j = i+1; j < distances[i].length; j++) {
				Double distance = distances[i][j];
				ArrayList<Integer> arc = new ArrayList<>();
				arc.add(i);
				arc.add(j);
				if (longestDistances.size()<numberOfArcs) {
					addArc(distance, arc, longestDistances, arcs, installationCount);
				}
				else {
					Double shortestDist = longestDistances.get(0);
					Double ijdist = getDistance(i,j, arcs);
					Double shortestI = getShortestDistance(i, arcs);
					Double shortestJ = getShortestDistance(j, arcs);
					if (ijdist != null && distance>ijdist
							&& installationCount.get(i) >= maxPerInstallation
							&& installationCount.get(j) >= maxPerInstallation) {
						removeArc(ijdist, longestDistances, arcs, installationCount);
						addArc(distance, arc, longestDistances, arcs, installationCount);
					}
					else if (shortestI != null
							&& distance > shortestI 
							&& installationCount.get(i) >= maxPerInstallation 
							&& installationCount.get(j) < maxPerInstallation) {
						removeArc(shortestI, longestDistances, arcs, installationCount);
						addArc(distance, arc, longestDistances, arcs, installationCount);
					}
					else if (shortestJ != null
							&& distance > shortestJ
							&& installationCount.get(i) < maxPerInstallation
							&& installationCount.get(j) >= maxPerInstallation){
						removeArc(shortestJ, longestDistances, arcs, installationCount);
						addArc(distance, arc, longestDistances, arcs, installationCount);
					}
					else if (installationCount.get(i) >= maxPerInstallation || installationCount.get(j) >= maxPerInstallation) {
						continue;
					}
					else if (distance > shortestDist) {
						removeArc(shortestDist, longestDistances, arcs, installationCount);
						addArc(distance, arc, longestDistances, arcs, installationCount);	
					}
				}
			}
		}
		System.out.println(longestDistances);
		return new ArrayList<ArrayList<Integer>>(arcs.values());
	}
	
	public void minInstallationsHeur(int minimumNumberOfInstallations, HashMap<Vessel, ArrayList<Voyage>> voyageSetByVessel) {
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			ArrayList<Voyage> removeList = new ArrayList<Voyage>();
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				if (voyage.getVisited().size()-2 < minimumNumberOfInstallations) {
					removeList.add(voyage);
				}
			}
			ArrayList<Voyage> newVoyages = voyageSetByVessel.get(vessel);
			newVoyages.removeAll(removeList);
			voyageSetByVessel.put(vessel, newVoyages);
		}
	}
	
	private ArrayList<Integer> getInstallationsWithTimeWindows() {
		ArrayList<Integer> installationsWithTimeWindows = new ArrayList<Integer>();
		for (int i = 1; i < installations.size()-1; i++) {
			Installation installation = installations.get(i);
			if (installation.getOpeningHour() != 0 || installation.getClosingHour() != 24) {
				installationsWithTimeWindows.add(installation.getNumber());
			}
		}
		return installationsWithTimeWindows;
	}
	
	public void minCapacityUsed(double capacityFraction, HashMap<Vessel, ArrayList<Voyage>> voyageSetByVessel) {
		ArrayList<Integer> installationsWithTimeWindows = getInstallationsWithTimeWindows();
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			ArrayList<Voyage> removeList = new ArrayList<Voyage>();
			int capacity = vessel.getCapacity();
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				if (voyage.getCapacityUsed() < (capacity*capacityFraction)){ //&&
						//!(voyageContainsTimewindows(voyage, installationsWithTimeWindows))) {
					removeList.add(voyage);
				}
			}
			ArrayList<Voyage> newVoyages = voyageSetByVessel.get(vessel);
			newVoyages.removeAll(removeList);
			voyageSetByVessel.put(vessel, newVoyages);
		}
	}
	
	private boolean voyageContainsTimewindows(Voyage voyage, ArrayList<Integer> installationsWithTimeWindows) {
		for (Integer installation : voyage.getVisited())
			if (installationsWithTimeWindows.contains(installation)) {
				return true;
		}
		return false;
	}
	
	
}
