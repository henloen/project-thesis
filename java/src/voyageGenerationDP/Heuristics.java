package voyageGenerationDP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Heuristics {
	
	private double[][] distances;
	
	public Heuristics(double[][] distances) {
		this.distances = distances;
	}
	
	public void removeLongestDistancePairs(int numberOfPairs, HashMap<Vessel, ArrayList<Voyage>> voyageSetByVessel) {
		ArrayList<ArrayList<Integer>> pairs = longestDistancePairs(numberOfPairs);
		for (Vessel vessel : voyageSetByVessel.keySet()) {
			ArrayList<Voyage> removeList = new ArrayList<Voyage>();
			for (Voyage voyage : voyageSetByVessel.get(vessel)) {
				for (ArrayList<Integer> pair : pairs) {
					if (voyage.getVisited().containsAll(pair)) {
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
	
	private ArrayList<ArrayList<Integer>> longestDistancePairs (int numberOfPairs) {
		HashMap<Double, ArrayList<Integer>> pairs = new HashMap<Double, ArrayList<Integer>>();
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
				}
				else {
					Double shortestDist = longestDistances.get(0);
					if (distance > shortestDist) {
						longestDistances.remove(0);
						pairs.remove(shortestDist);
						ArrayList<Integer> pair = new ArrayList<Integer>();
						pair.add(i);
						pair.add(j);
						longestDistances.add(distance);
						pairs.put(distance, pair);
						Collections.sort(longestDistances);
					};
				}
			}
		}
		return new ArrayList<ArrayList<Integer>>(pairs.values());
	}
	
	
}
