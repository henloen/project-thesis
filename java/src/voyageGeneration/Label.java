package voyageGeneration;

public class Label {
	private double cost;
	private int departureTime;
	private Installation currentInstallation;
	private int[] visited;
		
	public Label(double cost, int departureTime, Installation currentInstallation, int[] visited) {
		this.cost = cost;
		this.departureTime = departureTime;
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
	
}
