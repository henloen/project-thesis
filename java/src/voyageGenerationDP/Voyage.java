package voyageGenerationDP;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Voyage implements Comparable<Voyage> {
	
	private double cost,capacityUsed, departureTime;
	private int number;
	private ArrayList<Integer> visited;
	private static int numberOfVoyages= 0;
	
	public Voyage(double cost, double capacityUsed, double departureTime, ArrayList<Integer> visited) {
		super();
		this.cost = cost;
		this.capacityUsed = capacityUsed;
		this.departureTime = departureTime;
		this.visited = visited;
		numberOfVoyages++;
		this.number = numberOfVoyages;
	}
	
	public double getCost() {
		return cost;
	}
	public double getCapacityUsed() {
		return capacityUsed;
	}
	public double getDepartureTime() {
		return departureTime;
	}
	public int getNumber() {
		return number;
	}
	public ArrayList<Integer> getVisited() {
		return visited;
	}
	public static int getNumberOfVoyages() {
		return numberOfVoyages;
	}
	
	public String getFullText() {
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		String string = "number: " + number + "\t cost: " + numberFormat.format(cost) + "\t capacityUsed: " + numberFormat.format(capacityUsed) + "\t departureTime: " + departureTime + "\t visited: ";
		for (int i = 0; i < visited.size(); i++) {
			string += visited.get(i);
			if (i != (visited.size() - 1)) {
				string+="-";
			}
		}
		return string;
	}
	
	public String toString() {
		return ""+number;
	}

	public int compareTo(Voyage o) {
		return this.number - o.number;
	}

}
