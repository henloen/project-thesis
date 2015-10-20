package voyageGeneration;

public class Label {
	private double cost,capacityUsed;
	private int departureTime;
	private Installation currentInstallation;
	private int[] visited;
		

	public Label(double cost, int departureTime, double capacityUsed,
			Installation currentInstallation, int[] visited) {
		this.cost = cost;
		this.departureTime = departureTime;
		this.capacityUsed = capacityUsed;
		this.currentInstallation = currentInstallation;
		this.visited = visited;
	}

	public double getCost() {
		return cost;
	}

	public int getDepartureTime() {
		return departureTime;
	}

	public Installation getCurrentInstallation() {
		return currentInstallation;
	}

	public int[] getVisited() {
		return visited;
	}

	public double getCapacityUsed() {
		return capacityUsed;
	}
	
}
