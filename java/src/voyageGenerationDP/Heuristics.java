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
		ArrayList<ArrayList<Integer>> pairs = longestDistancePairs(numberOfPairs, maxPerInstallation);
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
	
	
	private ArrayList<ArrayList<Integer>> longestDistancePairs (int numberOfPairs, int maxPerInstallation) {
		HashMap<Double, ArrayList<Integer>> pairs = new HashMap<Double, ArrayList<Integer>>();
		HashMap<Integer, Integer> installationCount = new HashMap<Integer, Integer>();
		for (Installation installation : installations) {
			installationCount.put(installation.getNumber(), 0);
		}
		ArrayList<Double> longestDistances = new ArrayList<Double>();
		for (int i = 1; i < distances[0].length; i ++) {
			for (int j = i+1; j < distances[i].length; j++) {
				Double distance = distances[i][j];
				if (longestDistances.size()<numberOfPairs) {
					ArrayList<Integer> pair = new ArrayList<Integer>();
					pair.add(i);
					pair.add(j);
					longestDistances.add(distance);
					pairs.put(distance, pair);
					installationCount.put(i, installationCount.get(i)+1);
					installationCount.put(j, installationCount.get(j)+1);
					Collections.sort(longestDistances);
					System.out.println(longestDistances);
				}
				else {
					Double shortestDist = longestDistances.get(0);
					if (distance > shortestDist) {
						Double shortestDistanceI = shortestDistanceInst(i, pairs);
						Double shortestDistanceJ = shortestDistanceInst(j, pairs);
						if (installationCount.get(i)>=(maxPerInstallation-1) && distance > shortestDistanceI) {
							System.out.println(""+i + ": " + installationCount.get(i));
							longestDistances.remove(shortestDistanceI);
							ArrayList<Integer> oldPair = pairs.remove(shortestDistanceI);
							System.out.println("oldPair: " + oldPair);
							installationCount.put(oldPair.get(0), installationCount.get(oldPair.get(0))-1);
							installationCount.put(oldPair.get(1), installationCount.get(oldPair.get(1))-1);
							ArrayList<Integer> pair = new ArrayList<Integer>();
							pair.add(i);
							pair.add(j);
							longestDistances.add(distance);
							pairs.put(distance, pair);
							Collections.sort(longestDistances);
							installationCount.put(i, installationCount.get(i)+1);
							installationCount.put(j, installationCount.get(j)+1);
							System.out.println("test1");
						}
						else if (installationCount.get(j)>=(maxPerInstallation-1) && distance > shortestDistanceJ) {
							System.out.println(""+i + ": " + installationCount.get(i));
							longestDistances.remove(shortestDistanceJ);
							ArrayList<Integer> oldPair = pairs.remove(shortestDistanceJ);
							System.out.println("oldPair: " + oldPair);
							installationCount.put(oldPair.get(0), installationCount.get(oldPair.get(0))-1);
							installationCount.put(oldPair.get(1), installationCount.get(oldPair.get(1))-1);
							ArrayList<Integer> pair = new ArrayList<Integer>();
							pair.add(i);
							pair.add(j);
							longestDistances.add(distance);
							pairs.put(distance, pair);
							Collections.sort(longestDistances);
							installationCount.put(i, installationCount.get(i)+1);
							installationCount.put(j, installationCount.get(j)+1);
							System.out.println("test2");
						}
						else if (installationCount.get(i)>maxPerInstallation || installationCount.get(j)>maxPerInstallation) {
							continue;
						}
						else {
							longestDistances.remove(0);
							ArrayList<Integer> oldPair = pairs.remove(shortestDist);
							installationCount.put(oldPair.get(0), installationCount.get(oldPair.get(0))-1);
							installationCount.put(oldPair.get(1), installationCount.get(oldPair.get(1))-1);
							ArrayList<Integer> pair = new ArrayList<Integer>();
							pair.add(i);
							pair.add(j);
							longestDistances.add(distance);
							pairs.put(distance, pair);
							Collections.sort(longestDistances);
							installationCount.put(i, installationCount.get(i)+1);
							installationCount.put(j, installationCount.get(j)+1);
						}
					}
				}
			}
		}
		System.out.println(longestDistances);
		System.out.println(installationCount);
		return new ArrayList<ArrayList<Integer>>(pairs.values());
	}
	
	private Double shortestDistanceInst(Integer i, HashMap<Double, ArrayList<Integer>> pairs) {
		Double shortestDist = 999999999.9;
		for (Double distance : pairs.keySet()) {
			if (distance<shortestDist && pairs.get(distance).contains(i)) {
				shortestDist = distance;
			}
		}
		return shortestDist;
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
