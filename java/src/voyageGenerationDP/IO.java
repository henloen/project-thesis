package voyageGenerationDP;

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

import jxl.Sheet;
import jxl.Workbook;


public class IO {
	
	private String inputFileName, outputFileName;
	private int minNumberOfInstallations, maxNumberOfInstallations, numberOfNodes, numberOfInstallationAttributes, numberOfVessels, numberOfVesselAttributes, numberOfTimeWindows, minDuration, maxDuration, lengthOfPlanningPeriod, firstRowOfProblemInstance, firstColumnOfProblemInstance;
	private double loadFactor;
	private ArrayList<Integer> depotCapacity;
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
		this.depotCapacity = new ArrayList<Integer>();
		getParameters();
	}
	

	public void writeOutputToDataFile(ArrayList<Installation> installations, ArrayList<Vessel> vessels, ArrayList<Voyage> voyageSet, HashMap<Vessel,ArrayList<Voyage>> voyageSetByVessel, HashMap<Vessel, HashMap<Installation, ArrayList<Voyage>>> voyageSetByVesselAndInstallation, HashMap<Vessel, HashMap<Integer, ArrayList<Voyage>>> voyageSetByVesselAndDuration, HashMap<Integer, ArrayList<Installation>> installationSetsByFrequency,long executionTime, int removeLongestPairs) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(generateOutputFilename(numberOfTimeWindows-1, getNumberOfTotalVisits(installations), removeLongestPairs), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong when writing to output file");
		}
		writeSummary(writer, voyageSet, voyageSetByVessel, executionTime);
		writer.println("!------------------------------------------------------------------------------------------------------------------------");
		writeSimpleSets(writer, voyageSet, vessels, installations);
		writer.println("! Sets: ");
		writeRv(writer, vessels, voyageSetByVessel);
		writeRvi(writer,installations, vessels, voyageSetByVesselAndInstallation);
		writeRvl(writer, vessels, voyageSetByVesselAndDuration);
		writeNf(writer, installationSetsByFrequency);
		writeParameters(writer, voyageSet, installations, vessels);
		writer.close();
	}
	
	private void writeSummary(PrintWriter writer, ArrayList<Voyage> voyageSet, HashMap<Vessel,ArrayList<Voyage>> voyageSetByVessel, long executionTime) {
		DecimalFormat numberFormat = new DecimalFormat("0.00");
		writer.println("!Summary:");
		writer.println("!Execution time: " + numberFormat.format((double) executionTime/1000000000) + " seconds");
		int totalNumberOfVoyages = 0;
		for (Vessel v : voyageSetByVessel.keySet()) {
			totalNumberOfVoyages += voyageSetByVessel.get(v).size();
		}
		writer.println("!Total # voyages: " + totalNumberOfVoyages + " (" + voyageSet.size() + " unique)");
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
	}
	
	private void writeSimpleSets(PrintWriter writer, ArrayList<Voyage> voyageSet, ArrayList<Vessel> vessels, ArrayList<Installation> installations) {
		writer.println("nV : " + vessels.size());
		writer.println("nR : " + voyageSet.size());
		writer.println("nN : " + (installations.size() - 1)); //node 0 is the depot and should be excluded
		writer.println("nT : " + lengthOfPlanningPeriod);
		writer.println("minL : " + ((minDuration - 8) / 24)); // the max duration is converted from hours to days
		writer.println("maxL : " + ((maxDuration - 8) / 24)); // the max duration is converted from hours to days
		writer.print("\n");
	}
	
	private void writeRv(PrintWriter writer, ArrayList<Vessel> vessels, HashMap<Vessel,ArrayList<Voyage>> voyageSetByVessel) {
		writer.println("Rv : [");
		for (Vessel vessel : vessels) {
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
	}
	
	private void writeRvi(PrintWriter writer, ArrayList<Installation> installations, ArrayList<Vessel> vessels,  HashMap<Vessel, HashMap<Installation, ArrayList<Voyage>>> voyageSetByVesselAndInstallation ) {
		writer.println("Rvi : [");
		writer.print("!i : ");
		for (int j = 1; j < installations.size() ; j++) {
			writer.print(j);
			if (! (j == (installations.size() - 1))) {
				writer.print(", ");
			}
		}
		writer.println();
		for (int i = 0 ; i < vessels.size(); i++) {
			Vessel vessel = vessels.get(i);
			for (int j = 1; j < installations.size() ; j++) {//starts at 1 to exclude the depot
				Installation installation = installations.get(j);
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
				if (j != installations.size() - 1) {
					 writer.print(", ");
				}
			}
			writer.println("!Vessel: " + vessel.getName());
		}
		writer.println("] \n");
	}
	
	private void writeRvl(PrintWriter writer, ArrayList<Vessel> vessels,  HashMap<Vessel, HashMap<Integer, ArrayList<Voyage>>> voyageSetByVesselAndDuration) {
		writer.println("Rvl : [");
		writer.print("!l : ");
		List<Integer> durations = new ArrayList<Integer>();
		for (Vessel v : voyageSetByVesselAndDuration.keySet()) {
			for (Integer duration : voyageSetByVesselAndDuration.get(v).keySet()) {
				if (! durations.contains(duration)) {
					durations.add(duration);
				}
			}
		}
		Collections.sort(durations);
		for (int i = 0; i < durations.size(); i++) {
			writer.print(durations.get(i));
			if (! (i == (durations.size() - 1))) {
				writer.print(", ");
			}
		}
		writer.println();
		for (int i = 0 ; i < vessels.size(); i++) {
			Vessel vessel = vessels.get(i);
			for (int j = 0; j < durations.size(); j++) {
				Integer duration = durations.get(j);
				writer.print("[");
				List<Voyage> voyages = voyageSetByVesselAndDuration.get(vessel).get(duration);
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
			writer.println("!Vessel: " + vessel.getName());
		}
		writer.println("] \n");
	}
	
	private void writeNf(PrintWriter writer, HashMap<Integer, ArrayList<Installation>> installationSetsByFrequency) {
		ArrayList<Integer> frequencies = new ArrayList<Integer>(); 
		frequencies.addAll(installationSetsByFrequency.keySet());
		Collections.sort(frequencies);
		writer.println("minF : " + frequencies.get(0));
		writer.println("maxF : " + frequencies.get(frequencies.size()-1));
		writer.println("Nf : [");
		writer.print("!f : ");
		for (int i = 0; i < frequencies.size(); i++) {
			writer.print(frequencies.get(i));
			if (i != (frequencies.size() -1)) {
				writer.print(", ");
			}
		}
		writer.println();
		for (int i = 0; i < frequencies.size(); i++) {
			ArrayList<Installation> installationSet = installationSetsByFrequency.get(frequencies.get(i));
			writer.print("[");
			for (int j = 0; j < installationSet.size(); j++) {
				writer.print(installationSet.get(j));
				if (j != (installationSet.size() - 1)) {
					writer.print(", ");
				}
			}
			writer.print("]");
			if (i != (frequencies.size() - 1)) {
				writer.print(", ");
			}
		}
		writer.println();
		writer.println("]");
		writer.println();
	}
	
	private void writeParameters(PrintWriter writer, ArrayList<Voyage> voyageSet, ArrayList<Installation> installations, ArrayList<Vessel> vessels) {
		writer.println("! Parameters");
		writer.print("VoyageCost: [");
		for (int i = 0; i < voyageSet.size(); i ++){
			writer.print(Math.round(voyageSet.get(i).getCost()));
			if (i != voyageSet.size() - 1) {
				writer.print(", ");
			}
		}
		writer.println("]");
		writer.print("VoyageDuration: [");
		for (int i = 0; i < voyageSet.size(); i++) {
			writer.print((int)(voyageSet.get(i).getDepartureTime() - 8 )/ 24);//cast to int to remove .0 (the model expects int)
			if (i != voyageSet.size() - 1) {
				writer.print(", ");
			}
		}
		writer.println("]");
		writer.print("TimeCharterCost: [");
		for (int i = 0; i < vessels.size(); i++) {
			writer.print(vessels.get(i).getTimeCharterCost());
			if (i != vessels.size() - 1) {
				writer.print(", ");
			}
		}
		writer.println("]");
		writer.print("RequiredVisits: [");
		for (int i = 1; i < installations.size(); i++) { //Starts at 1 to exclude the depots
			writer.print(installations.get(i).getFrequency());
			if (i != (installations.size() - 1)) {
				writer.print(", ");
			}
		}
		writer.println("]");
		writer.print("NumberOfDaysAvailable: [");
		for (int i = 0; i < vessels.size(); i++) {
			writer.print(vessels.get(i).getNumberOfDaysAvailable());
			if (i != (vessels.size()-1)) {
				writer.print(", ");
			}
		}
		writer.println("]");
		writer.print("DepotCapacity: [");
		for (int i = 0; i < depotCapacity.size(); i++) {
			writer.print(depotCapacity.get(i));
			if (i != (depotCapacity.size() - 1)) {
				writer.print(", ");
			}
		}
		writer.println("]");
	}
	
	public ArrayList<Installation> getInstallations() {
		getInstallationsData();
		ArrayList<Installation> installations = new ArrayList<Installation>();
		for (int i=0;i<installationsData.length;i++) {
			String[] data = installationsData[i];
			String name = data[0];
			double openingHour = Double.parseDouble(data[1]);
			double closingHour = Double.parseDouble(data[2]);
			double demand = Integer.parseInt(data[3]) * loadFactor;//adjust the demand with the load factor
			int frequency = Integer.parseInt(data[4]);
			double serviceTime = Double.parseDouble(data[5]);
			Installation tempInstallation = new Installation(name, openingHour, closingHour, demand, frequency, serviceTime, i);
			installations.add(tempInstallation);
			if (openingHour != 0 || closingHour != 24) {//added for the printing of the solution 
				numberOfTimeWindows ++;
			}
		}
		return installations;
	}
	
	public ArrayList<Vessel> getVessels() {
		getVesselsData();
		ArrayList<Vessel> vessels = new ArrayList<Vessel>();
		for (int i=0;i<vesselsData.length;i++) {
			String[] data = vesselsData[i];
			String name = data[0];
			int capacity = Integer.parseInt(data[1]);
			int speed = Integer.parseInt(data[2]);
			int unitFuelCost  = Integer.parseInt(data[3]);
			double fuelConsumptionSailing = Double.parseDouble(data[4]);
			double fuelConsumptionDepot = Double.parseDouble(data[5]);
			double fuelConsumptionInstallation = Double.parseDouble(data[6]);
			int timeCharterCost= Integer.parseInt(data[7]);
			int numberOfDaysAvailable = Integer.parseInt(data[8]);
			Vessel tempVessel = new Vessel(name, capacity, speed, unitFuelCost, fuelConsumptionSailing, fuelConsumptionDepot, fuelConsumptionInstallation, timeCharterCost, numberOfDaysAvailable);
			vessels.add(tempVessel);
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
					installationsData[i][j] = sheet.getCell((firstColumnOfProblemInstance-1) + j,(firstRowOfProblemInstance-1)+ i).getContents();//add 16 because the installations start at row 17 
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
					vesselsData[i][j] = sheet.getCell(j,i+4).getContents();//add 4 because the installations start at row 5 
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
			Sheet installationSheet = workbook.getSheet(0);//installation parameters are expected in the first sheet
			int startColumnInstallation = 1;//the column of parameters 
			int startRowInstallation = 0;//the first row of parameters
			this.minNumberOfInstallations= Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation).getContents());
			this.maxNumberOfInstallations= Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 1).getContents());
			this.loadFactor = Double.parseDouble(installationSheet.getCell(startColumnInstallation,startRowInstallation + 2).getContents());
			this.minDuration = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 3).getContents());
			this.maxDuration = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 4).getContents());
			this.lengthOfPlanningPeriod = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 5).getContents());
			
			this.numberOfNodes = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 7).getContents());
			this.numberOfInstallationAttributes = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 8).getContents());
			this.firstRowOfProblemInstance = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 9).getContents());
			this.firstColumnOfProblemInstance = Integer.parseInt(installationSheet.getCell(startColumnInstallation,startRowInstallation + 10).getContents());
			
			Sheet vesselSheet = workbook.getSheet(1); //vessel parameters are expected in the second sheet
			int startColumnVessel = 1;
			int startRowVessel = 0;
			this.numberOfVessels = Integer.parseInt(vesselSheet.getCell(startColumnVessel,startRowVessel).getContents());;
			this.numberOfVesselAttributes = Integer.parseInt(vesselSheet.getCell(startColumnVessel,startRowVessel + 1).getContents());
			
			
			Sheet xpressSheet = workbook.getSheet(2); //parameters for the voyage based formulation is expected in sheet 3
			int startColumnXpress = 1;
			int startRowXpress = 0;
			for (int i = 0; i < lengthOfPlanningPeriod; i++) {
				depotCapacity.add(Integer.parseInt(xpressSheet.getCell(startColumnXpress+i, startRowXpress).getContents()));
			}
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
	
	public String generateOutputFilename(int numberOfTimeWindows, int totalVisits, int removeLongestPairs) {
		String fileName = outputFileName + getTodaysDate() + " " + (numberOfNodes-1) + "-" + numberOfTimeWindows + "-" + totalVisits;
		if (removeLongestPairs > 0) {
			fileName += " longestRemoved " + removeLongestPairs ;
		}
		return fileName + ".txt";
	}

	public int getMinNumberOfInstallations() {
		return minNumberOfInstallations;
	}
	
	public int getNumberOfTotalVisits(ArrayList<Installation> installations) {
		int totalVisits = 0;
		for (Installation installation : installations) {
			if (installation.getNumber() != 0) {
				totalVisits += installation.getFrequency();
			}
		}
		return totalVisits;
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

}
