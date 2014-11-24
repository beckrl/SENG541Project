
public class Recommender {
	public Recommender(String [] oldJarArray, String [] currentJarArray, String [] errorList, int AlgorithmSelection, String filePath){
		String [] Recommendation = new String[errorList.length];
		
		switch(AlgorithmSelection) {
			case 1:
				Recommendation = JarComparisonAlgorithm(oldJarArray, currentJarArray, errorList);
				break;
			case 2:
				Recommendation = EvolvedParametersAlgorithm(currentJarArray, errorList);
				break;
			case 3:
				Recommendation = EvolvedReturnTypeAlgorithm(currentJarArray, errorList);
				break;
		}
		
		outputToFile(filePath, Recommendation);
	}
	
	String [] JarComparisonAlgorithm(String [] oldJarArray, String [] currentJarArray, String [] errorList) {
		String [] Recommendation = new String[errorList.length];
		Boolean [] flag = new Boolean[errorList.length];
		
		for(int i = 0; i < errorList.length; i++) {
			//if (oldJarArray[i].substring
		}
		
		//compares old and currentJarArray by comparing substrings of each other
		//flag each of the currentJarArray for name changes but have same number of parameters
		//for each error in errorList, use the flagged items in currentJArArray to provide recommendations
		
		return Recommendation;
	}
	
	String [] EvolvedParametersAlgorithm(String [] currentJarArray, String [] errorList) {
		String [] Recommendation = new String[errorList.length];
		
		//compares errorList with currentJarArray
		//for each error in errorList, look for substring function name from currentJarArray
		//then look for same parameter type and number of parameters.
		//make recommendations for each error in errorList
		
		return Recommendation;
	}
	
	String [] EvolvedReturnTypeAlgorithm(String [] currentJarArray, String [] errorList) {
		String [] Recommendation = new String[errorList.length];
		
		//compares errorList with currentJarArray
		//for each error in errorList, look for substring function name from currentJarArray
		//then look for same return type and number of parameters.
		//make recommendations for each error in errorList
		
		return Recommendation;
	}
	

	
	void outputToFile(String filepath, String [] Recommendation) {
		//parses list of recommendation and outputs to specified file path
	}
	
}
