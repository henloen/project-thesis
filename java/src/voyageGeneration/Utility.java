package voyageGeneration;

public class Utility {
	
	private static double[][] distances = {
			{0,1,3},
			{1,0,2},
			{3,2,0}
			};
	
	public static double getDistance(Installation i1, Installation i2) {
		return distances[i1.getNumber()][i2.getNumber()];
	}

}
