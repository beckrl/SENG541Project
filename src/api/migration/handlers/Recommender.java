package api.migration.handlers;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class Recommender {
	private String [] Recommendation1; 
	private String [] Recommendation2; 
	private String [] Recommendation3;
	private Boolean parsed;
	
	public Recommender(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList, String AlgorithmSelection) throws JavaModelException{
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//	 - Based on AlgorithmSelections, create recommendations
	//	 - Initializes each Recommendation array to the size of the errorList
	//	 - Parses for the method name and parameters for each error in the list
	//	 - Based on AlgorithmSelectons, call the corresponding heuristics methods to create and return recommendations
	// 	 - Once finished, mark as parsed in order to call OutputRecommendations
	//-----------------------------------------------------------------------------------------------------------------------------------//	
		Recommendation1 = new String[errorList.length];
		Recommendation2 = new String[errorList.length];
		Recommendation3 = new String[errorList.length];
		
		//parse for the method name and parameters of each errors in the list
		for(int i = 0; i < errorList.length; i++) {
			String [] parse = errorList[i].split("\\s+");
			errorList[i] = parse[3];
		}
		
		//calls each of the heuristic recommendations depending on the Algorithm Selections
		if(AlgorithmSelection.contains("a"))
			Recommendation1 = JarComparisonAlgorithm(oldJarArray, currentJarArray, errorList);
		
		if(AlgorithmSelection.contains("b"))
			Recommendation2 = EvolvedParametersAlgorithm(currentJarArray, errorList);
			
		if(AlgorithmSelection.contains("c"))
			Recommendation3 = EvolvedReturnTypeAlgorithm(currentJarArray, errorList);
		
		parsed = true;
	}
	
	String [] JarComparisonAlgorithm(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList) {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the old and current jar files and compares them based on substrings
	//  - Any notable changes are flagged and are used to determine the recommendation for each error in the List
	//  - Returns an array of Recommendations based on the comparison Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		String [] Recommendation = new String[errorList.length];
		int [] flag = new int[currentJarArray.length];
		
		for(int i = 0; i < currentJarArray.length; i++)
			flag[i] = 0;
		
		//compares old and currentJarArray by comparing substrings of each other
		for(int i = 0; i < oldJarArray.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//flag each of the currentJarArray for name changes but have same number of parameters
				if (oldJarArray[i].getElementName().toLowerCase().equals(currentJarArray[j].getElementName().toLowerCase())) {
					flag[i]--;
				} else if (oldJarArray[i].getElementName().toLowerCase().contains(currentJarArray[j].getElementName().toLowerCase()) || 
							currentJarArray[i].getElementName().toLowerCase().contains(oldJarArray[j].getElementName().toLowerCase())){
					flag[i]++; 
				}
			}
		}
		
		//for each error in errorList, use the flagged items in currentJArArray to provide recommendations
		for(int i = 0; i < errorList.length; i++) {
			Boolean found = false;
			
			for(int j = 0; j < currentJarArray.length; j++) {
				if(flag[j] == 1 && errorList[i].contains(currentJarArray[i].getElementName().toLowerCase())) {
					found = true;
					Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[i].getElementName());
				}
			}
			
			if(!found)
				Recommendation[i] = recommendationMessage(errorList[i], "");
			else
				found = false; //reset
		}
			
		return Recommendation;
	}
	
	String [] EvolvedParametersAlgorithm(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the current jar and the errorList and compares them based on substring and number of parameters
	//  - Any matching substring as well as number and type of parameters are recommended
	//  - Returns an array of Recommendations based on the Parameters Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		String [] Recommendation = new String[errorList.length];
		
		//compares errorList with currentJarArray
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(currentJarArray[i].getElementName().toLowerCase()) || 
					currentJarArray[i].getElementName().toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						
						//then look for same parameter type and number of parameters.
						//make recommendations for each error in errorList
						if (sameParameters(errorList[i], currentJarArray[i]))
							Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[i].getElementName());
						else
							Recommendation[i] = recommendationMessage(errorList[i], "");
					
				}
			}
		}
		
		return Recommendation;
	}
	
	String [] EvolvedReturnTypeAlgorithm(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the current jar and the errorList and compares them based on substring and return type
	//  - Any matching substring as well as return types are recommended
	//  - Returns an array of Recommendations based on the Return Type Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		String [] Recommendation = new String[errorList.length];
		
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(currentJarArray[i].getElementName().toLowerCase()) || 
					currentJarArray[i].getElementName().toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						
						//then look for same return type
						//make recommendations for each error in errorList
						if (getReturnType(errorList[i]).toLowerCase().contains(getReturnType(currentJarArray[i].getSignature())))
								Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[i].getElementName());
						else
							Recommendation[i] = recommendationMessage(errorList[i], "");
					
				}
			}
		}
		
		return Recommendation;
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  Parsing Methods:
	//    - getMethodName (gets the error in the errorList and returns the method name)
	//	  - getParameters (gets the error in the errorList and returns a string array of all its parameters)
	// 	  - sameParameters(gets the error in the List and the IMethod of currentJar; returns a true or false if the parameters match)
	//	  - getReturnType (gets the return type of the error in the List or from the IMethod currentJar .getSigniture() string)
	//	  - recommendationMessage (gets the errorMethod and the recommendedMethod; Returns the appropriate recommendation message based on recommendationMethod string)
	//----------------------------------------------------------------------------------------------------------------------------------//
	
	static String getMethodName(String method) {
		String [] parse = method.split("(");
		return parse[0];
	}
	
	static String [] getParameters(String method) {
		String [] split = method.split("(");
		String temp = split[1];
		split = temp.split(")");
		temp = split[0];
		split = temp.split(",");
		
		return split;
	}
	
	static Boolean sameParameters(String errorMethod, IMethod cjMethod) throws JavaModelException {
		String [] emParameters = getParameters(errorMethod);
		String [] cjParameters = getParameters(cjMethod.getSignature());
		
		if(emParameters.length != cjParameters.length)
			return false;
		
		for (int i = 0; i < emParameters.length; i++) {
			if(!emParameters[i].toLowerCase().equals(cjParameters[i]))
				return false;
		}
		
		return true;
	}
	
	static String getReturnType(String ReturnType) {
		String [] split = ReturnType.split("/");
		
		if(split.length == 0)
			return ReturnType;
		else
			return split[split.length - 1];
	}
	
	static String recommendationMessage(String errorMethod, String methodReplacement) {
		if (methodReplacement.equals(""))
			return "No recommendations found to solve issue compilation issue with " + getMethodName(errorMethod) + ".";
		else
			return "Recommendation: Replace " + getMethodName(errorMethod) + " with " + methodReplacement + ".";
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Based on the Algorithm Selection, output Recommendations to MigrationHandler
	//-----------------------------------------------------------------------------------------------------------------------------------//	
	
	public void outputRecommendation(String AlgorithmSelection) {
		//parses list of recommendation and outputs to specified file path
		if (parsed) {
			if (AlgorithmSelection.contains("a")) {
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation1[i] + "\n");
			}
			if (AlgorithmSelection.contains("b")) {
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation2[i] + "\n");
			}
			if (AlgorithmSelection.contains("c")) {
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation3[i] + "\n");
			}
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------//
// My Code Ends Here
//-----------------------------------------------------------------------------------------------------------------------------------//
	
//	public static void Recommender(String[] oldJarArray, String[] newJarArray,
//		String[] errorlist, String algorithmselection) {
//		String [] Recommendation1 = new String[errorlist.length];
//		String [] Recommendation2 = new String[errorlist.length];
//		String [] Recommendation3 = new String[errorlist.length];
//		
//		//parse for the method name of each error in the list
//		for(int i = 0; i < errorlist.length; i++) {
//			String [] parse = errorlist[i].split("\\s+");
//			errorlist[i] = parse[3];
//		}
//		
//		if(algorithmselection.contains("a"))
//		//	Recommendation1 = jarComparisonAlgorithm(oldJarArray, newJarArray, errorlist);
//		if(algorithmselection.contains("b"))
//		//	Recommendation2 = evolvedParametersAlgorithm(newJarArray, errorlist);
//		if(algorithmselection.contains("c"))
//		//	Recommendation3 = evolvedReturnTypeAlgorithm(newJarArray, errorlist);
//		
//		//outputToFile(Recommendation1, Recommendation2, Recommendation3);
//	}
//
//	private static String[] evolvedReturnTypeAlgorithm(String[] newJarArray,
//			String[] errorList) {
//		String [] Recommendation = new String[errorList.length];
//		
//		for(int i = 0; i < errorList.length; i++) {
//			for(int j = 0; j < newJarArray.length; j++) {
//				//for each error in errorList, look for substring function name from currentJarArray
//				if(getMethodName(errorList[i]).toLowerCase().contains(newJarArray[i].toLowerCase()) || 
//					newJarArray[i].toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
//						
//						//then look for same return type
//						//make recommendations for each error in errorList
//						if (getReturnType(errorList[i]).toLowerCase().contains(getReturnType(MigrationHandler.parameterMethodNew[i])))
//								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray + "";
//						else
//							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
//					
//				}
//			}
//		}
//		
//		return Recommendation;
//	}
//
//	private static String[] evolvedParametersAlgorithm(String[] newJarArray,
//			String[] errorList) {
//		String [] Recommendation = new String[errorList.length];
//		
//		//compares errorList with currentJarArray
//		for(int i = 0; i < errorList.length; i++) {
//			for(int j = 0; j < newJarArray.length; j++) {
//				//for each error in errorList, look for substring function name from currentJarArray
//				if(getMethodName(errorList[i]).toLowerCase().contains(newJarArray[i].toLowerCase()) || 
//					newJarArray[i].toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
//						
//						//then look for same parameter type and number of parameters.
//						//make recommendations for each error in errorList
//						if (SameParameters(errorList[i], newJarArray[i]))
//								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray[i] + "";
//						else
//							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
//					
//				}
//			}
//		}
//		
//		return Recommendation;
//	}
//
//	private static boolean SameParameters(String errorMethod, String cjMethod) {
//		String [] emParameters = getParameters(errorMethod);
//		String [] cjParameters = getParameters(cjMethod);
//		
//		if(emParameters.length != cjParameters.length)
//			return false;
//		
//		for (int i = 0; i < emParameters.length; i++) {
//			if(!emParameters[i].toLowerCase().equals(cjParameters[i]))
//				return false;
//		}
//		
//		return true;
//	}
//
//	private static String[] jarComparisonAlgorithm(String[] oldJarArray,
//			String[] newJarArray, String[] errorList) {
//		String [] Recommendation = new String[errorList.length];
//		int [] flag = new int[errorList.length];
//		
//		for(int i = 0; i < errorList.length; i++)
//			flag[i] = 0;
//		
//		//compares old and currentJarArray by comparing substrings of each other
//		for(int i = 0; i < errorList.length; i++) {
//			for(int j = 0; j < errorList.length; j++) {
//				//flag each of the currentJarArray for name changes but have same number of parameters
//				if (oldJarArray[i].toLowerCase().equals(newJarArray[j])) {
//					flag[i]--;
//				} else if (oldJarArray[i].toLowerCase().contains(newJarArray[j].toLowerCase()) || 
//							newJarArray[i].toLowerCase().contains(oldJarArray[j].toLowerCase())){
//					flag[i]++; 
//				}
//			}
//		}
//		
//		//for each error in errorList, use the flagged items in currentJArArray to provide recommendations
//		for(int i = 0; i < errorList.length; i++) {
//			Boolean found = false;
//			
//			for(int j = 0; j < errorList.length; j++) {
//			
//				if(flag[j] == 1 && errorList[i].contains(newJarArray[i].toLowerCase())) {
//					found = true;
//					Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray[i] + "";
//				}
//				
//			}
//			
//			if(!found)
//				Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
//			else
//				found = false; // reset
//		}
//			
//		return Recommendation;
//	}
//	
//	public void ErrorFinder(String []errorList){
//		String error1= "The import "+ " cannot be resolved";
//		String error2= "The method is undenfined for the type";
//		
//		for(int i=0; i<errorList.length; i++){
//			//if(errorList[i].con)
//		}
//	}
	
}
