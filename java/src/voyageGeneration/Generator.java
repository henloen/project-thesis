package voyageGeneration;

import java.util.ArrayList;
import java.util.Arrays;

public class Generator {
	
	private ArrayList<Label> unexploredLabels, exploredLabels;
	private Installation[] installationSubset;
	private int[] installationNumbers;
	private Vessel vessel;
	private int maxDuration;

	public Generator(Installation[] installationSubset, Vessel vessel, int maxDuration) {
		this.vessel = vessel;
		this.maxDuration = maxDuration;
		unexploredLabels= new ArrayList<Label>();
		exploredLabels= new ArrayList<Label>();
		this.installationSubset = installationSubset;
		int[] visited = new int[installationSubset.length]; // initial list of visited installations, the index represents the sequence of visits
		Arrays.fill(visited, -1);
		Label initLabel = new Label(0, 16, installationSubset[0],visited); //first and last installation in the installationSet is the depot
		unexploredLabels.add(initLabel);
		createInstallationNumbers();
	}

	public void findCheapestVoyage() {
		while(! unexploredLabels.isEmpty()) {
			extendLabel(unexploredLabels)
		}
	}
	
	private void extendLabel(Label currentLabel) {
		ArrayList<Installation> possibleInstallations = getPossibleInstallations(currentLabel.getVisited());
		for (Installation installation : possibleInstallations) {
			extendLabelToInstallation(currentLabel,installation);
		}
		unexploredLabels.remove(currentLabel);
		exploredLabels.add(currentLabel);
		
	}
	
	private void extendLabelToInstallation(Label currentLabel, Installation nextInstallation) {
		Installation currentInstallation = currentLabel.getCurrentInstallation();
		int currentDepartureTime = currentLabel.getDepartureTime();
		double currentCost = currentLabel.getCost();
		int sailingTime = (int) Math.ceil((Utility.getDistance(currentInstallation, nextInstallation)/vessel.getSpeed()));
		int arrivalTime = currentDepartureTime + sailingTime;
		int todaysOpeningHour = nextInstallation.getTodaysOpeningHour(arrivalTime);
		int todaysClosingHour = nextInstallation.getTodaysClosingHour(arrivalTime);
		int waitingTime = 0;
		int tomorrowsOpeningHour = nextInstallation.getTodaysOpeningHour(arrivalTime+24);
		if (arrivalTime < todaysOpeningHour) {
			waitingTime = todaysOpeningHour - arrivalTime;
		}
		else if (arrivalTime >= todaysClosingHour) {
			waitingTime = tomorrowsOpeningHour - arrivalTime;
		}
		//assumption: no installations have a service time greater than one working day
		else if (arrivalTime + nextInstallation.getServiceTime() > todaysClosingHour) {
			waitingTime = tomorrowsOpeningHour - todaysClosingHour; //add a night
		}
		int nextDepartureTime = arrivalTime + nextInstallation.getServiceTime() + waitingTime;
		//check that the solution is feasible
		if (nextDepartureTime <= maxDuration) {
			double nextCost = currentCost + (sailingTime*vessel.getFuelCostSailing()) + ((waitingTime+nextInstallation.getServiceTime())*vessel.getFuelCostInstallation());
			Label newLabel = new Label(nextCost,nextDepartureTime,nextInstallation,getNextVisited(currentLabel, nextInstallation));
			unexploredLabels.add(newLabel);
		}
	}
	
	private int[] getNextVisited(Label currentLabel, Installation nextInstallation) {
		int[] nextVisited = Arrays.copyOf(currentLabel.getVisited(), currentLabel.getVisited().length);
		for (int i=0; i<nextVisited.length;i++) {
			if (nextVisited[i] == -1) {
				nextVisited[i] = nextInstallation.getNumber();
				return nextVisited;
			}
		}
		return nextVisited;
	}
	
	private ArrayList<Installation> getPossibleInstallations(int[] visited) {
		ArrayList<Installation> possibleInstallations = new ArrayList<Installation>();
		for (int i=0; i<installationNumbers.length;i++) {
			boolean hasBeenVisited = false;
			for (int j=0;j<visited.length;j++) {
				if (installationNumbers[i] == visited[j]) {
					hasBeenVisited = true;
					break;
				}
			}
			if (!hasBeenVisited) {
				possibleInstallations.add(installationSubset[i]);
			}
		}
		return possibleInstallations;
	}
	
	private void createInstallationNumbers() {
		installationNumbers = new int[installationSubset.length];
		for (int i=0;i<installationSubset.length;i++) {
			installationNumbers[i] = installationSubset[i].getNumber();
		}
	}
}
