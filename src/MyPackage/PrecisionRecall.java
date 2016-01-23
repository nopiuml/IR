package MyPackage;

public class PrecisionRecall {

	public double calculatePrecision(int relevant_retrieved, int retrieved){
		
		double precision = 0.0;
		precision = (double)relevant_retrieved/retrieved;
		return precision;
	}
	
	public double calculateRecall(int relevant_retrieved, int relevant){
		
		double recall = 0.0;
		recall = (double)relevant_retrieved/relevant;
		return recall;
	}
}
