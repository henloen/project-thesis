package voyageGeneration;

public class Utility {
	
	private static double[][] distances = {
			{1,2,3,4,5},
			{1,2,3,4,5},
			{1,2,3,4,5},
			{1,2,3,4,5},
			{1,2,3,4,5}
			};
	
	public static double getDistance(Installation i1, Installation i2) {
		return distances[i1.getNumber()][i2.getNumber()];
	}

}
