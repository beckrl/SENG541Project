package api.migration.handlers;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class Recommender {
	public Recommender(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList, String AlgorithmSelection, String filePath) throws JavaModelException{
		String [] Recommendation1 = new String[errorList.length];
		String [] Recommendation2 = new String[errorList.length];
		String [] Recommendation3 = new String[errorList.length];
		
		
		//parse for the method name of each error in the list
		for(int i = 0; i < errorList.length; i++) {
			String [] parse = errorList[i].split("\\s+");
			errorList[i] = parse[3];
		}
		
		if(AlgorithmSelection.contains("a"))
			Recommendation1 = JarComparisonAlgorithm(oldJarArray, currentJarArray, errorList);
		
		if(AlgorithmSelection.contains("b"))
			Recommendation2 = EvolvedParametersAlgorithm(currentJarArray, errorList);
			
		if(AlgorithmSelection.contains("c"))
			Recommendation3 = EvolvedReturnTypeAlgorithm(currentJarArray, errorList);
		
		//outputToFile(Recommendation1, Recommendation2, Recommendation3, filePath);
	}
	
	String [] JarComparisonAlgorithm(IMethod [] oldJarArray, IMethod [] currentJarArray, String [] errorList) {
		String [] Recommendation = new String[errorList.length];
		int [] flag = new int[errorList.length];
		
		for(int i = 0; i < errorList.length; i++)
			flag[i] = 0;
		
		//compares old and currentJarArray by comparing substrings of each other
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < errorList.length; j++) {
				//flag each of the currentJarArray for name changes but have same number of parameters
				if (oldJarArray[i].getElementName().toLowerCase().equals(currentJarArray[j])) {
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
			
			for(int j = 0; j < errorList.length; j++) {
			
				if(flag[j] == 1 && errorList[i].contains(currentJarArray[i].getElementName().toLowerCase())) {
					found = true;
					Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + currentJarArray[i].getElementName() + "";
				}
				
			}
			
			if(!found)
				Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
			else
				found = false; // reset
		}
			
		return Recommendation;
	}
	
	String [] EvolvedParametersAlgorithm(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
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
								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + currentJarArray[i].getElementName() + "";
						else
							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
					
				}
			}
		}
		
		return Recommendation;
	}
	
	String [] EvolvedReturnTypeAlgorithm(IMethod [] currentJarArray, String [] errorList) throws JavaModelException {
		String [] Recommendation = new String[errorList.length];
		
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < currentJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(currentJarArray[i].getElementName().toLowerCase()) || 
					currentJarArray[i].getElementName().toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						
						//then look for same return type
						//make recommendations for each error in errorList
						if (getReturnType(errorList[i]).toLowerCase().contains(getReturnType(currentJarArray[i].getSignature())))
								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + currentJarArray[i].getElementName() + "";
						else
							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
					
				}
			}
		}
		
		return Recommendation;
	}
	
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
	
	Boolean sameParameters(String errorMethod, IMethod cjMethod) throws JavaModelException {
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
	
	static void outputToFile(String [] Recommendation1, String [] Recommendation2, String [] Recommendation3) {
		//parses list of recommendation and outputs to specified file path
		MigrationHandler.textBox.append("");
	}

	public static void Recommender(String[] oldJarArray, String[] newJarArray,
			String[] errorlist, String algorithmselection) {
		String [] Recommendation1 = new String[errorlist.length];
		String [] Recommendation2 = new String[errorlist.length];
		String [] Recommendation3 = new String[errorlist.length];
		
		//parse for the method name of each error in the list
		for(int i = 0; i < errorlist.length; i++) {
			String [] parse = errorlist[i].split("\\s+");
			errorlist[i] = parse[3];
		}
		
		if(algorithmselection.contains("a"))
		//	Recommendation1 = jarComparisonAlgorithm(oldJarArray, newJarArray, errorlist);
		if(algorithmselection.contains("b"))
		//	Recommendation2 = evolvedParametersAlgorithm(newJarArray, errorlist);
		if(algorithmselection.contains("c"))
		//	Recommendation3 = evolvedReturnTypeAlgorithm(newJarArray, errorlist);
		
		outputToFile(Recommendation1, Recommendation2, Recommendation3);
	}

	private static String[] evolvedReturnTypeAlgorithm(String[] newJarArray,
			String[] errorList) {
		String [] Recommendation = new String[errorList.length];
		
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < newJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(newJarArray[i].toLowerCase()) || 
					newJarArray[i].toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						
						//then look for same return type
						//make recommendations for each error in errorList
						if (getReturnType(errorList[i]).toLowerCase().contains(getReturnType(MigrationHandler.parameterMethodNew[i])))
								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray + "";
						else
							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
					
				}
			}
		}
		
		return Recommendation;
	}

	private static String[] evolvedParametersAlgorithm(String[] newJarArray,
			String[] errorList) {
		String [] Recommendation = new String[errorList.length];
		
		//compares errorList with currentJarArray
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < newJarArray.length; j++) {
				//for each error in errorList, look for substring function name from currentJarArray
				if(getMethodName(errorList[i]).toLowerCase().contains(newJarArray[i].toLowerCase()) || 
					newJarArray[i].toLowerCase().contains(getMethodName(errorList[i]).toLowerCase())) {
						
						//then look for same parameter type and number of parameters.
						//make recommendations for each error in errorList
						if (SameParameters(errorList[i], newJarArray[i]))
								Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray[i] + "";
						else
							Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
					
				}
			}
		}
		
		return Recommendation;
	}

	private static boolean SameParameters(String errorMethod, String cjMethod) {
		String [] emParameters = getParameters(errorMethod);
		String [] cjParameters = getParameters(cjMethod);
		
		if(emParameters.length != cjParameters.length)
			return false;
		
		for (int i = 0; i < emParameters.length; i++) {
			if(!emParameters[i].toLowerCase().equals(cjParameters[i]))
				return false;
		}
		
		return true;
	}

	private static String[] jarComparisonAlgorithm(String[] oldJarArray,
			String[] newJarArray, String[] errorList) {
		String [] Recommendation = new String[errorList.length];
		int [] flag = new int[errorList.length];
		
		for(int i = 0; i < errorList.length; i++)
			flag[i] = 0;
		
		//compares old and currentJarArray by comparing substrings of each other
		for(int i = 0; i < errorList.length; i++) {
			for(int j = 0; j < errorList.length; j++) {
				//flag each of the currentJarArray for name changes but have same number of parameters
				if (oldJarArray[i].toLowerCase().equals(newJarArray[j])) {
					flag[i]--;
				} else if (oldJarArray[i].toLowerCase().contains(newJarArray[j].toLowerCase()) || 
							newJarArray[i].toLowerCase().contains(oldJarArray[j].toLowerCase())){
					flag[i]++; 
				}
			}
		}
		
		//for each error in errorList, use the flagged items in currentJArArray to provide recommendations
		for(int i = 0; i < errorList.length; i++) {
			Boolean found = false;
			
			for(int j = 0; j < errorList.length; j++) {
			
				if(flag[j] == 1 && errorList[i].contains(newJarArray[i].toLowerCase())) {
					found = true;
					Recommendation[i] = "Recommendation: Replace " + getMethodName(errorList[i]) + " with " + newJarArray[i] + "";
				}
				
			}
			
			if(!found)
				Recommendation[i] = "No recommendations found to solve issue complilation issue with " + getMethodName(errorList[i]);
			else
				found = false; // reset
		}
			
		return Recommendation;
	}
	
	public void ErrorFinder(String []errorList){
		String error1= "The import "+ " cannot be resolved";
		String error2= "The method is undenfined for the type";
		
		for(int i=0; i<errorList.length; i++){
			//if(errorList[i].con)
		}
	}
	
}
