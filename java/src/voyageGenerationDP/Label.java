package voyageGenerationDP;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class Label {
	
	private int visitedSum, labelNumber;
	private double cost,capacityUsed, departureTime;
	private Installation currentInstallation;
	private Label predecessor;
	private static int labelsGenerated = 0;
	
	
	
	
	public Label(int visitedSum, double cost,
			double capacityUsed, double departureTime,
			Installation currentInstallation, Label predecessor) {
		super();
		this.visitedSum = visitedSum;
		this.cost = cost;
		this.capacityUsed = capacityUsed;
		this.departureTime = departureTime;
		this.currentInstallation = currentInstallation;
		this.predecessor = predecessor;
		labelsGenerated++;
		this.labelNumber = Integer.valueOf(labelsGenerated);
	}

	public int getVisitedSum() {
		return visitedSum;
	}
	
	public void setVisitedSum(int visitedSum) {
		this.visitedSum = visitedSum;
	}
	public int getLabelNumber() {
		return labelNumber;
	}
	public void setLabelNumber(int labelNumber) {
		this.labelNumber = labelNumber;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getCapacityUsed() {
		return capacityUsed;
	}
	public void setCapacityUsed(double capacityUsed) {
		this.capacityUsed = capacityUsed;
	}
	public double getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(double departureTime) {
		this.departureTime = departureTime;
	}
	public Installation getCurrentInstallation() {
		return currentInstallation;
	}
	public void setCurrentInstallation(Installation currentInstallation) {
		this.currentInstallation = currentInstallation;
	}
	public Label getPredecessor() {
		return predecessor;
	}
	public void setPredecessor(Label predecessor) {
		this.predecessor = predecessor;
	}
	public String getFullText() {
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		String string = "label number: " + labelNumber + "\t cost: " + numberFormat.format(cost) + "\t capacityUsed: " + numberFormat.format(capacityUsed) + "\t departureTime: " + departureTime + "\t currentInstallation: " + currentInstallation + "\t visited: ";
		ArrayList<Installation> visitedInstallation = getVisitedInstallations();
		for (int i = 0; i < visitedInstallation.size(); i++) {
			string += visitedInstallation.get(i);
			if (i != (visitedInstallation.size() - 1)) {
				string+="-";
			}
		}
		return string;
	}
	
	public ArrayList<Installation> getVisitedInstallations() {
		ArrayList<Installation> visitedInstallations = new ArrayList<Installation>();
		if (predecessor != null) { //if the predecessor is null, the current installation is the depot
			visitedInstallations.addAll(predecessor.getVisitedInstallations()); //add all installations visited by the predecessor (in the same sequence)
		}
		visitedInstallations.add(currentInstallation); // add the current installation as the last element in the arrayList
		return visitedInstallations;
	};
	
	public ArrayList<Integer> getVisitedInstallationNumbers() {
		ArrayList<Integer> visitedInstallationNumbers = new ArrayList<Integer>();
		for (Installation i : getVisitedInstallations()) {
			visitedInstallationNumbers.add(i.getNumber());
		}
		return visitedInstallationNumbers;
	}

}
