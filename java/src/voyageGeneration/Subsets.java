package voyageGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Subsets {
	
	private HashMap<Integer, Installation> installationNumbers;
	
	public ArrayList<Installation[]> generateSubsets(Installation[] fullSet, int minInstallations, int maxInstallations) {
		installationNumbers = new HashMap<Integer,Installation>();
		for (int i = 0; i<fullSet.length;i++) {
			installationNumbers.put(fullSet[i].getNumber(),fullSet[i]);
		}
		List<Integer> originalNumbers = getNumbersFromInstallations(fullSet);
		Set<Set<Integer>> allPowerSets = powerSet(new HashSet<Integer>(originalNumbers));
		Set<Set<Integer>> validPowerSets = getValidPowerSets(allPowerSets, minInstallations, maxInstallations);
		ArrayList<Installation[]> installationSubsets = getInstallationSubsetsFromNumbers(validPowerSets);
		return installationSubsets;
	}
	
	public Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<Integer>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size()));
        for (Set<Integer> set : powerSet(rest)) {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }
	
	private Set<Set<Integer>> getValidPowerSets(Set<Set<Integer>> allPowerSets, int minInstallations, int maxInstallations) {
		Set<Set<Integer>> validPowerSets = new HashSet<Set<Integer>>();
		for (Set<Integer> set : allPowerSets) {
			if ((set.size() >= minInstallations) && (set.size() <= maxInstallations)) {
				validPowerSets.add(set);
			}
		}
		return validPowerSets;
	}
	
	private List<Integer> getNumbersFromInstallations(Installation[] installations) {
		List<Integer> numbersList = new ArrayList<Integer>();
		for (int i=1;i<installations.length;i++) {//start from index 1 to generate subset of installations without the depot
			numbersList.add(installations[i].getNumber());
		}
		return numbersList;
	}
	
	private ArrayList<Installation[]> getInstallationSubsetsFromNumbers(Set<Set<Integer>> powerSets) {
		ArrayList<Installation[]> installationSubsets = new ArrayList<Installation[]>();
		for (Set<Integer> set : powerSets) {
			Installation[] installationSubset = new Installation[set.size()+1]; //the installationSubset needs an e
			installationSubset[0] = installationNumbers.get(0);//Add depot as the first node
			int i = 0;
			for (Integer number : set) {
				installationSubset[i+1] = installationNumbers.get(number);
				i++;
			}
			installationSubsets.add(installationSubset);
		}
		return installationSubsets;
	}

}
