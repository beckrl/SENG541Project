package api.migration.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class Recommender {

	// Class Variables
	private List<IMethod> problemMethods;
	private List<String> recommendations;
	
	
	/*
	 * Constructor
	 */
	public Recommender(List<IMethod> oldJarMethods, List<IMethod> newJarMethods, List<String> errorList, JTextArea textBox) {
		// Maps each IProblem to an equivalent IMethod inside oldJarMethods and add to problemMethods list
		problemMethods = new ArrayList<IMethod>();
		for(String message : errorList) {
			String problemName = getProblemMethodName(message);
			String[] problemParameters = getProblemParameters(message);
			
			for(IMethod method : oldJarMethods) {
				if( problemName.equals(method.getElementName()) ) {				
					if( problemParameters.length == method.getNumberOfParameters() ) {
						problemMethods.add(method);
					}
				}
			}
		}
		
		System.out.println("Number of elements in problemMethods: " + problemMethods.size());
		
		// Clear textBox and print description
		textBox.setText("");
		textBox.append(writeDescription());
	}
	
	
	/*
	 * Executes the selected algorithms
	 */
	public void executeAlgorithms(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList, String algorithmSelection) throws JavaModelException {
		recommendations= new ArrayList<String>();
		
		if (algorithmSelection.contains("a")){
			/*
			 * Analyze cases where the name of the missing methods are given as hint, it should provide
			 * a more insightful recommendation considering the list of options is limited to a method
			 * with the same name, thus it's expected to have the same logic, otherwise we will recommend
			 * you to create a new method with a different name
			 */
			if (algorithmSelection.contains("b")){
				if(algorithmSelection.contains("c")){
					//checking for errors by analyzing methods name, return type and parameters
					name_parameter_returnComaprisonAlgorithm(newJarMethods, oldJarMethods, errorList);
				}
				else{
					//checking for errors by analyzing methods name and their parameters type
					name_parameterComparisonAlgorithm(newJarMethods, oldJarMethods, errorList);
				}
				
			}
			else if (algorithmSelection.contains("c")){
				//checking for errors by analyzing methods name and their return type
				name_returnComparisonAlgorithm(newJarMethods, oldJarMethods, errorList);
			}
			else{
				//checking for errors by analyzing only methods name
				nameComparisonAlgorithm(newJarMethods, oldJarMethods, errorList);
			}
		}
		else{
			/*
			 * Analyzing cases where the name of the missing method is not considered but only its
			 * parameter or return type, the recommendations on this case might be more inaccurate
			 * considering we are not given a hint of where to start looking at
			 */
			if(algorithmSelection.contains("b")){
				if(algorithmSelection.contains("c")){
					//checking for errors by analyzing methods by their return and parameter type
					parameter_returnComparison(newJarMethods, oldJarMethods, errorList);
				}
				else{
					//checking for errors by analyzing methods by their parameter type
					parameterComparisonAlgorithm(newJarMethods, oldJarMethods, errorList);
				}
			}
			else if(algorithmSelection.contains("c")){
				//checking errors for only return types
				returnTypeComparisonAlgorithm(newJarMethods, errorList);
			}
			else{
				//nothing has been chosen, it will display nothing has been chosen!
			}
		}
	}

	
	/*
	 * If both parameter and return type comparison heuristic is selected
	 */
	private void parameter_returnComparison(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Parameter & Return Type Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the method name might have changed.\n");
		recommendations.add("Consider using the name heuristic to see if better recommendations can be made.\n\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.equals(newMethodName) ){
					recommendations.add(printMethodInfo(method) + "\n");
					match = true;
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			recommendations.add("\nError Method Name: " + errorMethodName + "\n");
			recommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				if(errorList.get(i).contains("(int")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							for(int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
								if(oldJarMethods.get(oldJar).getReturnType().equals(newJarMethods.get(newJar).getReturnType())){
									recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses integer as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses integer as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(errorList.get(i).contains("()")){
					if(newJarMethods.get(newJar).getParameterTypes().length==0){
						recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that does not use parameters as well, this is a possible option to\n"
									+ "replace "+errorMethodName + " with.\n");
							break;
					}
				}
				else if(errorList.get(i).contains("(String")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("Ljava.lang.String;")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							for(int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
								if(oldJarMethods.get(oldJar).getReturnType().equals(newJarMethods.get(newJar).getReturnType())){
									recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses String as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("Ljava.lang.String;")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses String as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(errorList.get(i).contains("(double")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("D")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							for(int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
								if(oldJarMethods.get(oldJar).getReturnType().equals(newJarMethods.get(newJar))){
									recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses double as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("D")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							recommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses double as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(newJar== newJarMethods.size()-1){
					recommendations.add("The current version only handles problem with argument types of: \n"
							+ "void, double, int and String. Sorry for the inconvenience, but I can't support this\n"
							+ "type of problem.\n");
				}
			}
		}
		*/
	}


	/*
	 * If both name and parameter comparison heuristic is selected
	 */
	private void name_parameterComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Name & Parameter Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the return type might have changed.\n");
		recommendations.add("Only the simplest of renaming of methods are supported at this time.\n");
		recommendations.add("Consider using the return type heuristic to see if better recommendations can be made.\n\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.contains(newMethodName) || newMethodName.contains(problemName) ){
					if(problem.getReturnType().equals(method.getReturnType())) {
						recommendations.add(printMethodInfo(method) + "\n");
						match = true;
					}
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			recommendations.add("\nError Method Name: " + errorMethodName + "\n");
			recommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							if(newJarMethods.get(newJar).getParameterTypes().equals(oldJarMethods.get(oldJar).getParameterTypes())){
								recommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "As well this method has the same parameter in both versions.\n"
										+ "My guessing is this method has a different return type.\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							else{
								recommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "But the methods differ on the arguments. Most likely the problem can be fix by\n"
										+ "updating your arguments. By switching from: "+ oldJarMethods.get(oldJar).getParameterTypes() +" to: \n"
										+ newJarMethods.get(newJar).getParameterTypes()+"\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							
						}
					}
					else if(newJarMethods.get(newJar).getElementName()==errorMethodName && oldJar==oldJarMethods.size()-1){
						recommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						recommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
		*/
	}


	/*
	 * If both name and return type comparison heuristic is selected
	 */
	private void name_returnComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Name & Return Type Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the parameters might have changed.\n");
		recommendations.add("Only the simplest of renaming of methods are supported at this time.\n");
		recommendations.add("Consider using the parameter heuristic to see if better recommendations can be made.\n\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.contains(newMethodName) || newMethodName.contains(problemName) ){
					if(problem.getParameterTypes().equals(method.getParameterTypes())) {
						recommendations.add(printMethodInfo(method) + "\n");
						match = true;
					}
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			recommendations.add("\nError Method Name: " + errorMethodName + "\n");
			recommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							if(newJarMethods.get(newJar).getReturnType().equals(oldJarMethods.get(oldJar).getReturnType())){
								recommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "As well this method has the same return type in both versions.\n"
										+ "My guessing is this method has different arguments compared to the previous jar version.\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							else{
								recommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "But the methods differ on the return type. Most likely the problem can be fix by\n"
										+ "updating your return type. By switching from: "+ oldJarMethods.get(oldJar).getReturnType() +" to: \n"
										+ newJarMethods.get(newJar).getReturnType()+"\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							
						}
					}
					else if(newJarMethods.get(newJar).getElementName()==errorMethodName && oldJar==oldJarMethods.size()-1){
						recommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						recommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
		*/
	}


	/*
	 * If all heuristic is selected
	 */
	private void name_parameter_returnComaprisonAlgorithm(List<IMethod> newJarMethods, List <IMethod> oldJarMethods, 
			List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Name, Parameters & Return Type Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.contains(newMethodName) || newMethodName.contains(problemName) ){
					recommendations.add(printMethodInfo(method) + "\n");
					match = true;
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
				recommendations.add("You will have to investigate further to find a solution to the problem.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			name_parameter_returnRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			name_parameter_returnRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							if(newJarMethods.get(newJar).getReturnType().equals(oldJarMethods.get(oldJar).getReturnType())){
								if(newJarMethods.get(newJar).getParameterTypes().equals(oldJarMethods.get(oldJar).getReturnType())){
									name_parameter_returnRecommendations.add("The method"+errorMethodName+ " is identical on both jar versions\n"
											+ "with respect: method's name, arugments and return type. Your logical problem\n"
											+ "is elsewhere, please check your class name or privacy settings.\n");
									newJar= newJarMethods.size();
									oldJar= oldJarMethods.size();
								}
								else{
									name_parameter_returnRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
											+ "jar versions and it's generating an error whenever the program compiles.\n"
											+ "As well this method has the same return type in both versions.\n"
											+ "My guessing is this method has different arguments compared to the previous jar version.\n"
											+ "Please be causious and create a new method to implement your new logic, if you are \n"
											+ "not sure how this method behaves.\n");
									newJar= newJarMethods.size();
									oldJar= oldJarMethods.size();	
								}
							}
							else{
								name_parameter_returnRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "But the methods differ on the return type. Most likely the problem can be fix by\n"
										+ "updating your return type. By switching from: "+ oldJarMethods.get(oldJar).getReturnType() +" to: \n"
										+ newJarMethods.get(newJar).getReturnType()+"\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							
						}
					}
					else if(newJarMethods.get(newJar).getElementName()==errorMethodName && oldJar==oldJarMethods.size()-1){
						name_parameter_returnRecommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						name_parameter_returnRecommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
		*/
	}

	
	/*
	 * If only name comparison heuristic is selected
	 */
	private void nameComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Name Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the return type or parameters have changed.\n");
		recommendations.add("Only the simplest of renaming of methods are supported at this time.\n");
		recommendations.add("Consider using more heuristics to see if better recommendations can be made.\n\n");

		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.contains(newMethodName) || newMethodName.contains(problemName) ){
					if(problem.getReturnType().equals(method.getReturnType())) {
						if(problem.getParameterTypes().equals(method.getParameterTypes())) {
							recommendations.add(printMethodInfo(method) + "\n");
							match = true;
						}
					}
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			nameRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			nameRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							nameRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
									+ "jar versions and it's generating an error whenever the program compiles.\n"
									+ "My guessing is this method has either a new parameter types or a new return type.\n"
									+ "Please be causious and create a new method to implement your new logic, if you are \n"
									+ "not sure how this method behaves.\n");
							newJar= newJarMethods.size();
							oldJar= oldJarMethods.size();
						}
					}
					else if(newJarMethods.get(newJar).getElementName()==errorMethodName && oldJar==oldJarMethods.size()-1){
						nameRecommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						nameRecommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
		*/
	}
	
	
	/*
	 * If only parameter comparison heuristic is selected
	 */
	private void parameterComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Parameter Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the method name or return type have changed.\n");
		recommendations.add("Consider using more heuristics to see if better recommendations can be made.\n\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			String[] problemArgs = problem.getParameterTypes(); 
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.equals(newMethodName) ){
					if(problem.getReturnType().equals(method.getReturnType())) {
						recommendations.add(printMethodInfo(method) + "\n");
						match = true;
					}
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
		
		/*
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			parameterRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			parameterRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				if(errorList.get(i).contains("(int")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses integer as parameter as well, this is a possible option to\n"
									+ "replace "+errorMethodName + " with.\n");
							break;
						}
						else if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses integer as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(errorList.get(i).contains("()")){
					if(newJarMethods.get(newJar).getParameterTypes().length==0){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that does not use parameters as well, this is a possible option to\n"
									+ "replace "+errorMethodName + " with.\n");
							break;
					}
				}
				else if(errorList.get(i).contains("(String")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("Ljava.lang.String;")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that use String as parameters as well, this is a possible option to\n"
									+ "replace "+errorMethodName + " with.\n");
							break;
						}
						else if(parameter.equals("Ljava.lang.String;")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses String as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(errorList.get(i).contains("(double")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("D")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that use Double as parameters as well, this is a possible option to\n"
									+ "replace "+errorMethodName + " with.\n");
							break;
						}
						else if(parameter.equals("D")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses double as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(newJar== newJarMethods.size()-1){
					parameterRecommendations.add("The current version only handles problem with argument types of: \n"
							+ "void, double, int and String. Sorry for the inconvenience, but I can't support this\n"
							+ "type of problem.\n");
				}
			}
		}
		*/
	}
	
	
	/*
	 * If only return type comparison heuristic is selected
	 */
	private void returnTypeComparisonAlgorithm(List<IMethod> newJarMethods, List<String> errorList) throws JavaModelException {
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("Return Type Comparison Recommendations\n");
		recommendations.add("----------------------------------------------------------\n");
		recommendations.add("If no recommendations can be found, it is likely that the method name or parameters have changed.\n");
		recommendations.add("Consider using more heuristics to see if better recommendations can be made.\n");
		recommendations.add("Keep in mind that often when only a return type is changed, the issue is often a type mismatch.\n");
		recommendations.add("Often the answer might be to change the types in your code and not an actual method replacement.\n\n");
		
		for(IMethod problem : problemMethods) {
			String problemName = problem.getElementName();
			
			recommendations.add("\nError in Method:            " + printMethodInfo(problem) + "\n");
			recommendations.add("Recommendation(s):\n");
			
			boolean match = false;
			for(IMethod method : newJarMethods) {
				String newMethodName = method.getElementName();
				if( problemName.equals(newMethodName) ){
					if(problem.getParameterTypes().equals(method.getParameterTypes())) {
						recommendations.add(printMethodInfo(method) + "\n");
						match = true;
					}
				}
			}
			
			if(match == false) {
				recommendations.add("No recommendations could be determined.\n");
			}
		}
	}
	
	
	/*
	 * Prints recommendations to the textBox in the second pop-up window
	 */
	public void printRecommendations(String algorithmSelection, JTextArea textBox) {
		// Clear the textBox before printing
		textBox.setText("");
		
		if( algorithmSelection.contains("a") || algorithmSelection.contains("b") || algorithmSelection.contains("c") ) {
			for(String line: recommendations){
				textBox.append(line);
			}
		}
		else {
			//nothing has been chosen!
			recommendations.add("----------------------------------------------------------\n");
			textBox.append("You haven't checked off any heuristics. Please do so in order to get some recommendations.\n");
			recommendations.add("----------------------------------------------------------\n\n");
			textBox.append(writeDescription());
		}
	}
	
	
	/*
	 * Returns the method name given the message of an IProblem
	 * Example:
	 * 		The method setDate(int) from the type Date is deprecated -> setDate
	 *		The method getDate() is undefined for the type MyClass -> getDate
	 */
	private String getProblemMethodName(String errorMessage) {
		String [] parse = errorMessage.split("\\s+");
		String temp = "";
		
		for(int i = 2; i < parse.length; i++)
			temp += parse[i] + " ";
	
		parse = temp.split("\\)");
		
		return parse[0].split("\\(")[0];
	}
	
	
	/*
	 * Returns a String array of the parameter types in an IProblem message
	 */
	private String[] getProblemParameters(String message) {
		String[] split = message.split("\\(");
		String temp = split[1];
		split = temp.split("\\)");
		temp = split[0];
		split = temp.split(", ");
		
		if( split.length == 1 && split[0].equals("") ) {
			return new String[0];
		}
		else {
			return split;
		}
	}
	
	
	/*
	 * Returns the shortened parameter type for an Imethod parameter
	 * Example: Ljava.lang.String; returns String
	 */
	private String parseType(String parameter) throws Exception {
		if(parameter.startsWith("Z"))
			return "boolean";
		else if(parameter.startsWith("B"))
			return "byte";
		else if(parameter.startsWith("C"))
			return "char";
		else if(parameter.startsWith("S"))
			return "short";
		else if(parameter.startsWith("I"))
			return "int";
		else if(parameter.startsWith("J"))
			return "long";
		else if(parameter.startsWith("F"))
			return "float";
		else if(parameter.startsWith("D"))
			return "double";
		else if(parameter.startsWith("V"))
			return "void";
		else if(parameter.startsWith("L")){
			String[] tmp = parameter.split("\\.");
	
			String result = tmp[tmp.length-1];
			result = result.replace(";", "");
	
			return result;
		}
		else
			throw new Exception();
	}
	
	
	
	/*
	 * Returns a String containing the info for an Imethod in the format:
	 * 		returnType methodName(parameters, parameters,)
	 */
	private String printMethodInfo(IMethod method) {
		String methodName = method.getElementName();
		String returnType = "";
		try {
			returnType = parseType(method.getReturnType());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] args = method.getParameterTypes();
		
		String result;
		result = returnType + " " + methodName + "(";
		
		if(args.length == 0) {
			result += ")";
		}
		else if(args.length == 1) {
			try {
				result += parseType(args[0]) + ")";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			for(String arg : args) {
				try {
					result += parseType(arg) + ",";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			result += ")";
		}
		
		return result;
	}
	
	
	/*
	 * 
	 */
	private String writeDescription() {
		String string = "";
		
		string += "Name Comparison:\n";
		string += "By selecting this heuristic, we will try to find methods that have different (but similar) names as the problem method.\n";
		string += "This will assume that the return type and parameters haven't changed - only the name.\n";
		string += "The plugin only supports simple name changes that have a portion of the original names:\n";
		string += "Compatible: helloWorld() changes to printHelloWorld() and vice versa\n";
		string += "Incompatible: helloWorld() changes to helloToTheWorld()\n";
		
		string += "\nParameter Comparison:\n";
		string += "By selecting this heuristic, we will try to find methods that have different parameters as the problem method.\n";
		string += "This will assume that the method name and return type haven't changed - only the parameters.\n";
		
		string += "\nReturn Type Comparison:\n";
		string += "By selecting this heuristic, we will try to find methods that have different return type as the problem method.\n";
		string += "This will assume that the method name and parameters haven't changed - only the return type.\n";
		
		string += "\nIf you think a combination of these things might have changed, consider selecting morethan one heuristic.\n";
		string += "Selecting all three heuritics was find any methods that have similar names and any return type and/or parameters.\n";
		
		return string;
	}

}
