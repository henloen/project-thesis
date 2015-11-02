package voyageGeneration;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jxl.Sheet;
import jxl.Workbook;
/*
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
*/

public class IO {
	private String inputFileName, outputFileName;
	private int minNumberOfInstallations, maxNumberOfInstallations, numberOfNodes, numberOfInstallationAttributes, numberOfVessels, numberOfVesselAttributes, numberOfTimeWindows, minDuration, maxDuration, lengthOfPlanningPeriod;
	private double loadFactor;
	private String[][] installationsData;//each row consists of name, openingHour, closingHour, demand, frequency, serviceTime
	private String[][] vesselsData;//each row consists of name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumtionDepot, fuelConsumptionInstallation
	private String[][] distancesData;//corresponds to the distance matrix.
	
	/*note that it's assumed that the distance matrix is fully dense, i.e. you can't send in the
	 *  installation subset 1,2 and 4 because it would load the distances for 1, 2 and 3.
	 *  This can be fixed by editing getDistancesData(), but is not implemented at the moment
	 */
	

	public IO(String inputFileName, String outputFileName) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		getParameters();
	}
	

	public void writeOutputToDataFile(Installation[] installations, Vessel[] vessels, Set<Voyage> voyageSet, HashMap<Vessel,List<Voyage>> voyageSetByVessel, HashMap<Vessel, HashMap<Installation, List<Voyage>>> voyageSetByVesselAndInstallation, HashMap<Vessel, HashMap<Integer, List<Voyage>>> voyageSetByVesselAndDuration,long executionTime) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(generateOutputFilename(numberOfTimeWindows-1, true), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when writing to output file");
		}
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		writer.println("!Summary:");
		writer.println("!Execution time: " + numberFormat.format((double) executionTime/1000000000) + " seconds");
		writer.println("!Total # voyages: " + voyageSet.size());
		writer.println("!Number of installations (excluding depot): " + (numberOfNodes - 1));
		writer.println("!Number of time windows: " + (numberOfTimeWindows-1));//-1 because the depot has opening hours as well
		writer.println("!Number of vessels: " + voyageSetByVessel.keySet().size() + "\n");
		writer.println("!Min. # installations: " + minNumberOfInstallations);
		writer.println("!Max. number of installations: " + maxNumberOfInstallations);
		writer.println("!Min. duration: " + minDuration);
		writer.println("!Max. duration: " + maxDuration + "\n");
		for (Vessel v : voyageSetByVessel.keySet()) {
			writer.println("!Name: " + v.getName() + "\t # voyages: " + voyageSetByVessel.get(v).size());
		}
		writer.println("!------------------------------------------------------------------------------------------------------------------------");
		writer.println("nV : " + vessels.length);
		writer.println("nR : " + voyageSet.size());
		writer.println("nN : " + (installations.length - 1)); //node 0 is the depot and should be excluded
		writer.println("minF : " + minNumberOfInstallations);
		writer.println("maxF : " + maxNumberOfInstallations);
		writer.println("nT : " + lengthOfPlanningPeriod);
		writer.println("minL : " + ((minDuration - 8) / 24)); // the max duration is converted from hours to days
		writer.println("maxL : " + ((maxDuration - 8) / 24)); // the max duration is converted from hours to days
		writer.print("\n");
		
		writer.println("Rv : [");
		for (int i = 0; i < vessels.length; i++) {
			Vessel vessel = vessels[i];
			writer.print("[");
			List<Voyage> voyages = voyageSetByVessel.get(vessel);
			for (int j = 0; j < voyages.size() - 1; j++) { //don't want to print ", " after the last voyage, therefore we use -1
				Voyage l = voyages.get(j);
				writer.print(l + ", ");
			}
			writer.print(voyages.get(voyages.size()-1) + "]");
			writer.println("!Vessel: " + vessel.getName());
		}
		writer.println("] \n");
		
		writer.println("Rvi : [");
		for (int i = 0 ; i < vessels.length; i++) {
			Vessel vessel = vessels[i];
			writer.print("[");
			for (int j = 1; j < installations.length ; j++) {//starts at 1 to exclude the depot
				Installation installation = installations[j];
				writer.print("[");
				List<Voyage> voyages = voyageSetByVesselAndInstallation.get(vessel).get(installation);
				for (int k = 0; k < voyages.size(); k++) {
					Voyage l = voyages.get(k);
					writer.print(l);
					if (k!=voyages.size() - 1) {
						 writer.print(", ");
					}
				}
				writer.print("]");
				if (j != installations.length - 1) {
					 writer.print(", ");
				}
			}
			writer.print("]");
			writer.println("!Vessel: " + vessel.getName());
		}
		writer.println("] \n");
		
		/*
		writer.println("Rvl : [");
		for (int i = 0 ; i < vessels.length; i++) {
			Vessel vessel = vessels[i];
			List<Integer> durations = new ArrayList<Integer>();
			durations.addAll(voyageSetByVesselAndDuration.get(vessel).keySet());
			Collections.sort(durations);
			writer.print("[");
			for (int j = 0; j < durations.size(); j++) {
				Integer duration = durations.get(j);
				writer.print("[");
				List<Voyage> voyages = voyageSetByVesselAndInstallation.get(vessel).get(duration);
				for (int k = 0; k < voyages.size(); k++) {
					Voyage l = voyages.get(k);
					writer.print(l);
					if (k!=voyages.size() - 1) {
						 writer.print(", ");
					}
				}
				writer.print("]");
				if (j != durations.size() - 1) {
					 writer.print(", ");
				}
			}
			writer.print("]");
			writer.println("!Vessel: " + vessel.getName());
		}
		writer.println("]");*/
		
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
	
	//used for debugging
	public void writeOutputToTextFile(HashMap<Vessel,List<Voyage>> voyageSet, HashMap<Vessel, HashMap<Installation, List<Voyage>>> voyageSetByVesselAndInstallation, HashMap<Vessel, HashMap<Integer, List<Voyage>>> voyageSetByVesselAndDuration,long executionTime) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(generateOutputFilename(numberOfTimeWindows-1, false), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when writing to output file");
		}
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		writer.println("!Summary:");
		writer.println("!Execution time: " + numberFormat.format((double) executionTime/1000000000) + " seconds");
		int totalNumberOfVoyages = 0;
		for (Vessel v : voyageSet.keySet()) {
			totalNumberOfVoyages += voyageSet.get(v).size();
		}
		writer.println("!Total # voyages: " + totalNumberOfVoyages);
		writer.println("!Number of installations (excluding depot): " + (numberOfNodes - 1));
		writer.println("!Number of time windows: " + (numberOfTimeWindows-1));//-1 because the depot has opening hours as well
		writer.println("!Number of vessels: " + voyageSet.keySet().size() + "\n");
		writer.println("!Min. # installations: " + minNumberOfInstallations);
		writer.println("!Max. number of installations: " + maxNumberOfInstallations);
		writer.println("!Min. duration: " + minDuration);
		writer.println("!Max. duration: " + maxDuration + "\n");
		for (Vessel v : voyageSet.keySet()) {
			writer.println("!Name: " + v.getName() + "\t # voyages: " + voyageSet.get(v).size());
		}
		writer.println("!------------------------------------------------------------------------------------------------------------------------");
		for (Vessel v : voyageSet.keySet()) {
			writer.println("!Vessel: " + v.getName());
			writer.println("Voyages: ");
			for (Voyage l : voyageSet.get(v)) {
				writer.println(l.getFullText());
			}
			writer.println("------------------------------------------------------------------------------------------------------------------------");
		}
		
		writer.println("------------------------------------------------------------------------------------------------------------------------");
		for (Vessel v : voyageSetByVesselAndInstallation.keySet()) {
			writer.println("Vessel: " + v.getName());
			for (Installation i : voyageSetByVesselAndInstallation.get(v).keySet()) {
				writer.println("Installation: " + i.getName() + " (" + i.getNumber() + ")");
				writer.println("Voyages: ");
				for (Voyage l : voyageSetByVesselAndInstallation.get(v).get(i)) {
					writer.println(l.getFullText());
				}
			}
			writer.println("------------------------------------------------------------------------------------------------------------------------");
		}
		
		writer.println("------------------------------------------------------------------------------------------------------------------------");
		for (Vessel v : voyageSetByVesselAndDuration.keySet()) {
			writer.println("Vessel: " + v.getName());
			for (Integer d : voyageSetByVesselAndDuration.get(v).keySet()) {
				writer.println("Duration: " + d);
				writer.println("Voyages: ");
				for (Voyage l : voyageSetByVesselAndDuration.get(v).get(d)) {
					writer.println(l.getFullText());
				}
			}
			writer.println("------------------------------------------------------------------------------------------------------------------------");
		}
		
	}
	
	
	public Vessel[] getVessels() {
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
		try {
			Workbook workbook = Workbook.getWorkbook(new File(inputFileName));
			Sheet sheet = workbook.getSheet(0);//installations data is expected in the first sheet
			for (int i=0; i<numberOfNodes;i++) {
				for (int j=0;j<numberOfInstallationAttributes;j++) {
					installationsData[i][j] = sheet.getCell(j,i+18).getContents();//add 18 because the installations start at row 19 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the installations data from file");
		}
	}
	
	public void getVesselsData() {
		vesselsData = new String[numberOfVessels][numberOfVesselAttributes];
		try {
			Workbook workbook = Workbook.getWorkbook(new File(inputFileName));
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
		try {
			Workbook workbook = Workbook.getWorkbook(new File(inputFileName));
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
	
	public void getParameters() {
		try {
			Workbook workbook = Workbook.getWorkbook(new File(inputFileName));
			Sheet sheet = workbook.getSheet(0);//parameters is expected in the first sheet
			int startColumn = 1;//the column of parameters 
			int startRow = 0;//the first row of parameters
			this.minNumberOfInstallations= Integer.parseInt(sheet.getCell(startColumn,startRow).getContents());
			this.maxNumberOfInstallations= Integer.parseInt(sheet.getCell(startColumn,startRow + 1).getContents());
			this.numberOfNodes = Integer.parseInt(sheet.getCell(startColumn,startRow + 2).getContents());
			this.numberOfInstallationAttributes = Integer.parseInt(sheet.getCell(startColumn,startRow + 3).getContents());
			this.numberOfVessels = Integer.parseInt(sheet.getCell(startColumn,startRow + 4).getContents());;
			this.numberOfVesselAttributes = Integer.parseInt(sheet.getCell(startColumn,startRow + 5).getContents());
			this.loadFactor = Double.parseDouble(sheet.getCell(startColumn,startRow + 6).getContents());
			this.minDuration = Integer.parseInt(sheet.getCell(startColumn,startRow + 7).getContents());
			this.maxDuration = Integer.parseInt(sheet.getCell(startColumn,startRow + 8).getContents());
			this.lengthOfPlanningPeriod = Integer.parseInt(sheet.getCell(startColumn,startRow + 9).getContents());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the parameters from file");
		}
		
	}
	
	public String getTodaysDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public String generateOutputFilename(int numberOfTimeWindows, boolean dataOutput) {
		String fileName = outputFileName + getTodaysDate() + " " + (numberOfNodes-1) + "-" + (numberOfTimeWindows);
		if (! dataOutput) {
			fileName += "Text";
		}
		return fileName + ".txt";
	}

	public int getMinNumberOfInstallations() {
		return minNumberOfInstallations;
	}

	public int getMaxNumberOfInstallations() {
		return maxNumberOfInstallations;
	}

	public int getMinDuration() {
		return minDuration;
	}

	public int getMaxDuration() {
		return maxDuration;
	}
	
	/*
	private void printDoubleArray(Object[][] array) {
		for (int i = 0; i<array.length;i++) {
			for (int j = 0; j<array[i].length;j++) {
				System.out.println(array[i][j]);
			}
		}
	}
	
	
	public void writeOutputToExcel () {
		System.out.println("test1");
		try {
			Workbook workbook = Workbook.getWorkbook(new File(inputFileName));
			WritableWorkbook copy = Workbook.createWorkbook(new File(outputFileName + "output.xls"), workbook);
			WritableSheet sheet = copy.getSheet(2);
			WritableCell cell = sheet.getWritableCell(5,2);
			System.out.println("test2");
			System.out.println(cell.getType());
			if (cell.getType() == CellType.NUMBER) {
				System.out.println("test3");
				jxl.write.Number n = (jxl.write.Number) cell;
				n.setValue(10);
			};
			copy.write();
			copy.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when loading the vessels data from file");
		}
	}*/


}
