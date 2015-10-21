package voyageGeneration;

import java.io.File;

import jxl.Sheet;
import jxl.Workbook;

public class IO {
	private String fileName;
	private int numberOfInstallations, numberOfInstallationAttributes, numberOfVessels, numberOfVesselAttributes;
	private String[][] installationsData;//each row consists of name, openingHour, closingHour, demand, frequency, serviceTime
	private String[][] vesselsData;//each row consists of name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumtionDepot, fuelConsumptionInstallation
	private String[][] distancesData;//corresponds to the distance matrix.
	
	/*note that it's assumed that the distance matrix is fully dense, i.e. you can't send in the
	 *  installation subset 1,2 and 4 because it would load the distances for 1, 2 and 3.
	 *  This can be fixed by editing getDistancesData(), but is not implemented at the moment
	 */
	

	public IO(int numberOfInstallations, int numberOfInstallationAttributes,
			int numberOfVessels, int numberOfVesselAttributes, String fileName) {
		this.fileName = fileName;
		this.numberOfInstallations = numberOfInstallations;
		this.numberOfInstallationAttributes = numberOfInstallationAttributes;
		this.numberOfVessels = numberOfVessels;
		this.numberOfVesselAttributes = numberOfVesselAttributes;
	}

	
	public Installation[] getInstallations() {
		getInstallationsData();
		Installation[] installations = new Installation[installationsData.length];
		for (int i=0;i<installationsData.length;i++) {
			String[] data = installationsData[i];
			String name = data[0];
			double openingHour = Double.parseDouble(data[1]);
			double closingHour = Double.parseDouble(data[2]);
			int demand = Integer.parseInt(data[3]);
			int frequency = Integer.parseInt(data[4]);
			double serviceTime = Double.parseDouble(data[5]);
			installations[i] = new Installation(name, openingHour, closingHour, demand, frequency, serviceTime, i);
		}
		return installations;
	}
	
	
	public Vessel[] getVessels () {
		getVesselsData();
		Vessel[] vessels = new Vessel[vesselsData.length];
		for (int i=0;i<vesselsData.length;i++) {
			String[] data = vesselsData[i];
			String name = data[0];
			int capacity = Integer.parseInt(data[1]);
			int speed = Integer.parseInt(data[2]);
			int unitFuelCost  = Integer.parseInt(data[3]);
			double fuelConsumptionSailing = Double.parseDouble(data[4]);
			double fuelConsumptionDepot = Double.parseDouble(data[5]);
			double fuelConsumptionInstallation = Double.parseDouble(data[6]);
			vessels[i] = new Vessel(name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumptionDepot, fuelConsumptionInstallation);
		}
		return vessels;
	}
	
	public double[][] getDistances() {
		getDistancesData();
		double[][] distances = new double[distancesData.length][distancesData.length];
		for (int i=0;i<distancesData.length;i++) {
			for (int j=0;j<distancesData[i].length;j++) {
				distances[i][j] = Double.parseDouble(distancesData[i][j]);
			}
		}
		return distances;
	}
	
	
	public void getInstallationsData(){
		installationsData = new String[numberOfInstallations][numberOfInstallationAttributes];
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
			Sheet sheet = workbook.getSheet(0);//installations data is expected in the first sheet
			for (int i=0; i<numberOfInstallations;i++) {
				for (int j=0;j<numberOfInstallationAttributes;j++) {
					installationsData[i][j] = sheet.getCell(j,i+13).getContents();//add 13 because the installations start at row 14 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the installations data from file");
		}
	}
	
	public void getVesselsData() {
		vesselsData = new String[numberOfVessels][numberOfVesselAttributes];
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
			Sheet sheet = workbook.getSheet(1);//vessel data is expected in the second sheet
			for (int i=0; i<numberOfVessels;i++) {
				for (int j=0;j<numberOfVesselAttributes;j++) {
					vesselsData[i][j] = sheet.getCell(j,i+2).getContents();//add 2 because the installations start at row 3 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the vessels data from file");
		}
	}
	
	public void getDistancesData() {
		distancesData = new String[numberOfInstallations][numberOfInstallations];
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
			Sheet sheet = workbook.getSheet(4);//installations data is expected in the fifth sheet
			for (int i=0; i<numberOfInstallations;i++) {
				for (int j=0;j<numberOfInstallations;j++) {
					distancesData[i][j] = sheet.getCell(j+3,i+2).getContents();//add 3 and 2 because the data starts in D3  
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the distance data from file");
		}
	}
	
	private void printDoubleArray(Object[][] array) {
		for (int i = 0; i<array.length;i++) {
			for (int j = 0; j<array[i].length;j++) {
				System.out.println(array[i][j]);
			}
		}
	}
	

}
