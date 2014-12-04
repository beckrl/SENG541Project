package api.migration.handlers;

import java.util.Arrays;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class RecommenderOld {
/*
	private String [] Recommendation1; 
	private String [] Recommendation2; 
	private String [] Recommendation3;
	private String sonnyRecommends1;
	private String sonnyRecommends2;
	private String sonnyRecommends3;
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
		sonnyRecommends1="";	
		sonnyRecommends2="";
		sonnyRecommends3="";
		//parse for the method name and parameters of each errors in the list
		for(int i = 0; i < errorList.length; i++) {
			String [] parse = errorList[i].split("\\s+");
			
			if (ValidError(parse[0])) 
				errorList[i] = getErrorMethod(errorList[i]);
			else 
				errorList[i] = "*ignore*";
		}
		
		//calls each of the heuristic recommendations depending on the Algorithm Selections
		if(AlgorithmSelection.contains("a"))
			//Recommendation1 = JarComparisonAlgorithm(oldJarArray, currentJarArray, errorList);
			sonnyRecommends1= jarComparisonAlgorithmSonny(oldJarArray, currentJarArray, errorList);
		if(AlgorithmSelection.contains("b"))
			//Recommendation2 = EvolvedParametersAlgorithm(currentJarArray, errorList);
			sonnyRecommends2= evolvedParametersAlgorithmSonny(currentJarArray, errorList);
		if(AlgorithmSelection.contains("c"))
			//Recommendation3 = EvolvedReturnTypeAlgorithm(currentJarArray, errorList);
			sonnyRecommends3= evolvedReturnTypeAlgorithmSonny(currentJarArray, errorList);
		parsed = true;
	}
	
	String jarComparisonAlgorithmSonny(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList) {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the old and current jar files and compares them based on substrings
	//  - Any notable changes are flagged and are used to determine the recommendation for each error in the List
	//  - Returns an array of Recommendations based on the comparison Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		//String[] Recommendation = new String[errorList.length];
		String Recommendation="";
		int errorNumb=1;
		for (int oldJar= 0; oldJar< MigrationHandler.oldJarArray.length; oldJar++){
			for (int currentJar=0; currentJar<MigrationHandler.newJarArray.length; currentJar++){
				if(MigrationHandler.oldJarArray[oldJar].equals(MigrationHandler.newJarArray[currentJar])){
					currentJar=MigrationHandler.newJarArray.length-1;	//goes to the next value to check, the previous element is found
				}
				else if ((MigrationHandler.oldJarArray[oldJar].compareTo(MigrationHandler.newJarArray[currentJar])!=0 ) && 
						(currentJar==(MigrationHandler.newJarArray.length-1)))
				{
					Recommendation+= "Error Number["+errorNumb+"] \n"
					+ "The method: "+ MigrationHandler.oldJarArray[oldJar]+" is currently not being implemented in the new project,\n "
					+ "most likely it was renamed or discard, please make sure the logic of the project is not affected anyhow.\n";
					errorNumb++;
				}
			}	
		}
		
		return Recommendation;
	}
	
	String [] JarComparisonAlgorithm(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList) {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the old and current jar files and compares them based on substrings
	//  - Any notable changes are flagged and are used to determine the recommendation for each error in the List
	//  - Returns an array of Recommendations based on the comparison Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		String[] Recommendation = new String[errorList.length];

		
		int [] flag = new int[currentJarArray.length];
		
		for(int i = 0; i < currentJarArray.length; i++)
			flag[i] = 0;
		
		//compares old and currentJarArray by comparing substrings of each other
		for(int i = 0; i < oldJarArray.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//flag each of the currentJarArray for name changes but have same number of parameters
				if (oldJarArray[i].getElementName().toLowerCase().equals(currentJarArray[j].getElementName().toLowerCase())) {
					flag[j]--;
				} else if (oldJarArray[i].getElementName().toLowerCase().contains(currentJarArray[j].getElementName().toLowerCase()) || 
							currentJarArray[j].getElementName().toLowerCase().contains(oldJarArray[i].getElementName().toLowerCase())){
					flag[j]++; 
				}
			}
		}
				
		System.out.println("currentJarArray all;");
		for (int i =0; i < currentJarArray.length; i++)
		{
			System.out.println(currentJarArray[i].getElementName());
		}
		System.out.println("\ncurrentJarArray flag = 0;");
		for (int i =0; i < currentJarArray.length; i++)
		{
			if(flag[i] == 0)
				System.out.println(currentJarArray[i].getElementName());
		}
		System.out.println("\ncurrentJarArray flag = 1;");
		for (int i =0; i < currentJarArray.length; i++)
		{
			if(flag[i] == 1)
				System.out.println(currentJarArray[i].getElementName());
		}
		
		//for each error in errorList, use the flagged items in currentJArArray to provide recommendations
		for(int i = 0; i < errorList.length; i++) {
			Boolean found = false;
			
			for(int j = 0; j < currentJarArray.length; j++) {
				if(flag[j] == 1 && errorList[i].contains(currentJarArray[j].getElementName().toLowerCase())) {
					found = true;
					Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[j].getElementName());
					break;
				}
			}
			
			if(!found)
				Recommendation[i] = recommendationMessage(errorList[i], "");
			else
				found = false; //reset
		}
			
		return Recommendation;
	}
	
	String evolvedParametersAlgorithmSonny(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
		//-----------------------------------------------------------------------------------------------------------------------------------//
		//  - Takes in the IMethod array of the current jar and the errorList and compares them based on substring and number of parameters
		//  - Any matching substring as well as number and type of parameters are recommended
		//  - Returns an array of Recommendations based on the Parameters Algorithm
		//-----------------------------------------------------------------------------------------------------------------------------------//
		String Recommendation="";
		int errorNumb=1;
		for (int oldJar= 0; oldJar< MigrationHandler.oldJarArray.length; oldJar++){
			for (int currentJar=0; currentJar<MigrationHandler.newJarArray.length; currentJar++){
				if(MigrationHandler.oldJarArray[oldJar].equals(MigrationHandler.newJarArray[currentJar])){
					if(MigrationHandler.parameterMethodOld[oldJar].length!=MigrationHandler.parameterMethodNew[currentJar].length){
						Recommendation+= "Error Number["+errorNumb+"]\n"
						+ "The method: "+ MigrationHandler.oldJarArray[oldJar]+" currently doesn't have matching number of arguments with its "
						+ "identical named method within the new project. Please verify this is not affecting anyhow the logic of the project.\n";
						errorNumb++;
					}
					else{
						for(int arg=0; arg<MigrationHandler.parameterMethodOld[oldJar].length; arg++){
							if(MigrationHandler.parameterMethodOld[oldJar][arg].compareTo(MigrationHandler.parameterMethodNew[currentJar][arg])!=0){
								Recommendation+= "Error Number["+errorNumb+"] \n"
								+ "The method: "+ MigrationHandler.oldJarArray[oldJar]+" currently doesn't have matching arguments type with its identical\n"
								+ "named method within the new project. Please verify this is not affecting anyhow the logic of the project.\n"
								+ "The argument "+arg+" is the one causing the problem.\n";
								errorNumb++;
							}
						}
					}
					
				}
			}	
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
		Boolean found = false;
		
		//compares errorList with currentJarArray
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(currentJarArray[j].getElementName().toLowerCase()) || 
					currentJarArray[j].getElementName().toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						found = true;
					
						//then look for same parameter type and number of parameters.
						//make recommendations for each error in errorList
						if (sameParameters(errorList[i], currentJarArray[j]))
							Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[j].getElementName());
						else
							Recommendation[i] = recommendationMessage(errorList[i], "");
					
				}
			}
			
			if(!found){
				Recommendation[i] = recommendationMessage(errorList[i], "");
			} else found = false;
				
		}
		
		return Recommendation;
	}

	String evolvedReturnTypeAlgorithmSonny(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  - Takes in the IMethod array of the current jar and the errorList and compares them based on substring and return type
	//  - Any matching substring as well as return types are recommended
	//  - Returns an array of Recommendations based on the Return Type Algorithm
	//-----------------------------------------------------------------------------------------------------------------------------------//
		String Recommendation = "";
		int errorNumb=1;
		for (int oldJar= 0; oldJar< MigrationHandler.oldJarArray.length; oldJar++){
			for (int currentJar=0; currentJar<MigrationHandler.newJarArray.length; currentJar++){
				if(MigrationHandler.oldJarArray[oldJar].equals(MigrationHandler.newJarArray[currentJar])){
					if(MigrationHandler.returnTypeOld[oldJar].compareTo(MigrationHandler.returnTypeNew[currentJar])!=0){
						Recommendation+= "Error Number["+errorNumb+"] \n"
						+ "The method: "+ MigrationHandler.oldJarArray[oldJar]+" currently doesn't have matching return type with its identical\n"
						+ "named method within the new project. Please verify this is not affecting anyhow the logic of the project.\n";
						errorNumb++;
					}
					
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
		Boolean found = false;
		
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(currentJarArray[j].getElementName().toLowerCase()) || 
					currentJarArray[j].getElementName().toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						found = true;
					
						//then look for same return type
						//make recommendations for each error in errorList
						if (getReturnType(errorList[i]).toLowerCase().contains(getReturnType(currentJarArray[j].getSignature())))
								Recommendation[i] = recommendationMessage(errorList[i], currentJarArray[j].getElementName());
						else
							Recommendation[i] = recommendationMessage(errorList[i], "");
					
				}
			}
			
			if(!found){
				Recommendation[i] = recommendationMessage(errorList[i], "");
			} else found = false;
		}
		
		return Recommendation;
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//  Parsing Methods:
	//    - getMethodName (gets the error in the errorList and returns the method name)
	//	  - getParameters (gets the error in the errorList and returns a string array of all its parameters)
	// 	  - sameParameters(gets the error in the List and the IMethod of currentJar; returns a true or false if the parameters match)
	//	  - getReturnType (gets the return type of the error in the List or from the IMethod currentJar .getSigniture() string)
	//	  - validError    (gets the errorType and returns true or false whether the errorType is supported for recommendation)
	//	  - recommendationMessage (gets the errorMethod and the recommendedMethod; Returns the appropriate recommendation message based on recommendationMethod string)
	//----------------------------------------------------------------------------------------------------------------------------------//
	
	static String getMethodName(String method) {
		String [] parse = method.split("\\(");
		return parse[0];
	}
	
	static String [] getParameters(String method) {
		String [] split = method.split("\\(");
		String temp = split[1];
		split = temp.split("\\)");
		temp = split[0];
		
		if(temp.contains(";"))
			split = temp.split(";");
		else
			split = temp.split(", ");
		
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
		String [] temp = ReturnType.split("\\)");
		
		if(split.length == 1)
			return temp[1];
		else
			return split[split.length - 1];
	}
	
	static Boolean ValidError(String errorType) {
		if (errorType.equals("Pb(100)") || errorType.equals("Pb(103)") 
		 || errorType.equals("Pb(115)")|| errorType.equals("Pb(133)")) {
			return true;
		} else 
			return false;
	}
	
	static String getErrorMethod(String errorMessage) {
		String [] parse = errorMessage.split("\\s+");
		String temp = "";
		
		for(int i = 3; i < parse.length; i++)
			temp += parse[i] + " ";
	
		parse = temp.split("\\)");
		
		return parse[0] + ")";
	}
	
	static String recommendationMessage(String errorMethod, String methodReplacement) {
		if (errorMethod.equals("*ignore*"))
			return "No recommendations supported for this error Type";
		else if(methodReplacement.equals(""))
			return "No recommendations found to solve issue compilation issue with " + getMethodName(errorMethod) + ".";
		else if(errorMethod.equals("MethodNotFound")){
			return "The method: "+methodReplacement+"is not longer available within the new project, make sure the system is not implementing it.";
		}
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
				MigrationHandler.textBox.append("Recommendation 1: Jar Comparison Algorthim\n");
				MigrationHandler.textBox.append("------------------------------------------\n");
				
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation1 + "\n");
				
				MigrationHandler.textBox.append(sonnyRecommends1);
				MigrationHandler.textBox.append("------------------------------------------\n\n");
			}
			
			if (AlgorithmSelection.contains("b")) {
				MigrationHandler.textBox.append("Recommendation 2: Name and Parameter Algorthim\n");
				MigrationHandler.textBox.append("------------------------------------------\n");
				
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation2[i] + "\n");
				
				MigrationHandler.textBox.append(sonnyRecommends2);
				MigrationHandler.textBox.append("------------------------------------------\n\n");
			}
			
			if (AlgorithmSelection.contains("c")) {
				MigrationHandler.textBox.append("Recommendation 3: Name and Return Type Algorthim\n");
				MigrationHandler.textBox.append("------------------------------------------\n");
				
				for(int i = 0; i < Recommendation1.length; i++)
					MigrationHandler.textBox.append(Recommendation1[i] + "\n");
				
				MigrationHandler.textBox.append(sonnyRecommends3);
				MigrationHandler.textBox.append("------------------------------------------\n\n");
			}
		}
	}
*/
}


