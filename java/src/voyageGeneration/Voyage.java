package voyageGeneration;

import java.text.DecimalFormat;

public class Voyage implements Comparable<Voyage> {
	
	private double cost,capacityUsed, departureTime;
	private int number;
	private int[] visited;
	private static int numberOfVoyages= 0;
	
	public Voyage(double cost, double capacityUsed, double departureTime, int[] visited) {
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
	public int[] getVisited() {
		return visited;
	}
	public static int getNumberOfVoyages() {
		return numberOfVoyages;
	}
	
	public String getFullText() {
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		String string = "number: " + number + "\t cost: " + numberFormat.format(cost) + "\t capacityUsed: " + numberFormat.format(capacityUsed) + "\t departureTime: " + departureTime + "\t visited: ";
		for (int i=0;i<visited.length-1;i++) {
			string+=visited[i] + "-";
		}
		string+= visited[visited.length-1];
		return string;
	}
	
	public String toString() {
		return ""+number;
	}

	public int compareTo(Voyage o) {
		return this.number - o.number;
	}

}
