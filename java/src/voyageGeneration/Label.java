package voyageGeneration;

public class Label {
	private double cost,capacityUsed, departureTime;
	private int number;
	private Installation currentInstallation;
	private int[] visited;
		
	private static int labelsGenerated = 0;


	public Label(double cost, double departureTime, double capacityUsed,
			Installation currentInstallation, int[] visited) {
		this.cost = cost;
		this.departureTime = departureTime;
		this.capacityUsed = capacityUsed;
		this.currentInstallation = currentInstallation;
		this.visited = visited;
		labelsGenerated++;
		this.number = Integer.valueOf(labelsGenerated);
		
	}

	public double getCost() {
		return cost;
	}

	public double getDepartureTime() {
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
	
	public String toString() {
		return ""+number;
	}
	
	public String getFullText() {
		String string = "number: " + number + ", cost: " + cost + ", capacityUsed: " + capacityUsed + ", departureTime: " + departureTime + ", currentInstallation: " + currentInstallation + ", visited: ";
		for (int i=0;i<visited.length;i++) {
			string+=visited[i];
		}
		return string;
	}
	
}
