package voyageGeneration;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

public class IO {
	private String inputFileName, outputFileName;
	private int numberOfNodes, numberOfInstallationAttributes, numberOfVessels, numberOfVesselAttributes, numberOfTimeWindows;
	private double loadFactor;
	private String[][] installationsData;//each row consists of name, openingHour, closingHour, demand, frequency, serviceTime
	private String[][] vesselsData;//each row consists of name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumtionDepot, fuelConsumptionInstallation
	private String[][] distancesData;//corresponds to the distance matrix.
	
	/*note that it's assumed that the distance matrix is fully dense, i.e. you can't send in the
	 *  installation subset 1,2 and 4 because it would load the distances for 1, 2 and 3.
	 *  This can be fixed by editing getDistancesData(), but is not implemented at the moment
	 */
	

	public IO(int numberOfNodes, int numberOfInstallationAttributes,
			int numberOfVessels, int numberOfVesselAttributes, double loadFactor, String inputFileName, String outputFileName) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		this.numberOfNodes = numberOfNodes;
		this.numberOfInstallationAttributes = numberOfInstallationAttributes;
		this.numberOfVessels = numberOfVessels;
		this.numberOfVesselAttributes = numberOfVesselAttributes;
		this.loadFactor = loadFactor;
	}

	public void writeSolutionToFile(HashMap<Vessel,List<Label>> voyageSet, long executionTime, int minDuration, int maxDuration, int minNumberOfInstallations, int maxNumberOfInstallations) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(generateOutputFilename(numberOfTimeWindows-1), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when writing to output file");
		}
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		writer.println("Summary:");
		writer.println("Execution time: " + numberFormat.format((double) executionTime/1000000000) + " seconds");
		int totalNumberOfVoyages = 0;
		for (Vessel v : voyageSet.keySet()) {
			totalNumberOfVoyages += voyageSet.get(v).size();
		}
		writer.println("Total # voyages: " + totalNumberOfVoyages);
		writer.println("Number of installations (excluding depot): " + (numberOfNodes - 1));
		writer.println("Number of time windows: " + (numberOfTimeWindows-1));//-1 because the depot has opening hours as well
		writer.println("Number of vessels: " + voyageSet.keySet().size());
		writer.println("Min. # installations: " + minNumberOfInstallations);
		writer.println("Max. number of installations: " + maxNumberOfInstallations);
		writer.println("Min. duration: " + minDuration);
		writer.println("Max. duration: " + maxDuration);
		for (Vessel v : voyageSet.keySet()) {
			writer.println("Name: " + v.getName() + "\t # voyages: " + voyageSet.get(v).size());
		}
		writer.println("------------------------------------------------------------------------------------------------------------------------");
		for (Vessel v : voyageSet.keySet()) {
			writer.println("Vessel: " + v.getName());
			writer.println("Voyages: ");
			for (Label l : voyageSet.get(v)) {
				writer.println(l.getFullText());
			}
			writer.println("------------------------------------------------------------------------------------------------------------------------");
		}
		writer.close();
	}
	
	
	public Installation[] getInstallations() {
		getInstallationsData();
		Installation[] installations = new Installation[installationsData.length];
		for (int i=0;i<installationsData.length;i++) {
			String[] data = installationsData[i];
			String name = data[0];
			double openingHour = Double.parseDouble(data[1]);
			double closingHour = Double.parseDouble(data[2]);
			double demand = Integer.parseInt(data[3]) * loadFactor;//adjust the demand with the load factor
			int frequency = Integer.parseInt(data[4]);
			double serviceTime = Double.parseDouble(data[5]);
			installations[i] = new Installation(name, openingHour, closingHour, demand, frequency, serviceTime, i);
			if (openingHour != 0 || closingHour != 24) {//added for the printing of the solution 
				numberOfTimeWindows ++;
			}
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
		installationsData = new String[numberOfNodes][numberOfInstallationAttributes];
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(new File(inputFileName));
			Sheet sheet = workbook.getSheet(0);//installations data is expected in the first sheet
			for (int i=0; i<numberOfNodes;i++) {
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
			workbook = Workbook.getWorkbook(new File(inputFileName));
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
		distancesData = new String[numberOfNodes][numberOfNodes];
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(new File(inputFileName));
			Sheet sheet = workbook.getSheet(4);//installations data is expected in the fifth sheet
			for (int i=0; i<numberOfNodes;i++) {
				for (int j=0;j<numberOfNodes;j++) {
					distancesData[i][j] = sheet.getCell(j+3,i+2).getContents();//add 3 and 2 because the data starts in D3  
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the distance data from file");
		}
	}
	
	public String getTodaysDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public String generateOutputFilename(int numberOfTimeWindows) {
		return outputFileName + getTodaysDate() + " " + (numberOfNodes-1) + "-" + (numberOfTimeWindows) + ".txt";
	}
	
//	private void printDoubleArray(Object[][] array) {
//		for (int i = 0; i<array.length;i++) {
//			for (int j = 0; j<array[i].length;j++) {
//				System.out.println(array[i][j]);
//			}
//		}
//	}

}
