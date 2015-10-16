package voyageGeneration;

public class Label {
	private double cost;
	private int departureTime;
	private Installation currentInstallation;
		

	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public int getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(int departureTime) {
		this.departureTime = departureTime;
	}
	public Installation getCurrentInstallation() {
		return currentInstallation;
	}
	public void setCurrentInstallation(Installation currentInstallation) {
		this.currentInstallation = currentInstallation;
	}
}
