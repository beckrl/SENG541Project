package api.migration.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class Recommender {

	// Class Variables
	private List<IMethod> changedMethods;
	private List<IMethod> problemMethods;
	
	//variables storing recommendations when method's name is being analyze 
	private List<String> nameRecommendations;
	private List<String> name_parameterRecommendations;
	private List<String> name_parameter_returnRecommendations;
	private List<String> name_returnRecommendations;
	
	//variables storing recommendations when method's name is not being analyze
	private List<String> return_parameterRecommendations;
	private List<String> returnTypeRecommendations;
	private List<String> parameterRecommendations;
	
	/*
	 * Constructor
	 */
	public Recommender(List<IMethod> oldJarMethods, List<IMethod> newJarMethods, List<String> errorList) {
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
		
		// Compare all methods in new Jar with old Jar.
		// Any methods that are new or have been modified since the old Jar are added to changedMethods list.
		changedMethods = new ArrayList<IMethod>();
		for(int i=0; i < newJarMethods.size(); i++) {
			Boolean found = false;
			for(int j=0; j < oldJarMethods.size(); j++) {
				if( newJarMethods.get(i).getElementName().equals( oldJarMethods.get(j).getElementName() ) ) {
					found = true;
					break;		//no need to keep going through the whole list if we already know it exists!
				}
			}
			
			if(found == false) {
				changedMethods.add( newJarMethods.get(i) );
			}
		}
	}
	
	
	/*
	 * Executes the selected algorithms
	 */
	public void executeAlgorithms(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList, String algorithmSelection) throws JavaModelException {
		nameRecommendations= new ArrayList<String>();
		name_parameterRecommendations= new ArrayList<String>();
		name_parameter_returnRecommendations= new ArrayList<String>();
		name_returnRecommendations= new ArrayList<String>();
		return_parameterRecommendations= new ArrayList<String>();
		returnTypeRecommendations= new ArrayList<String>();
		parameterRecommendations= new ArrayList<String>();
		
		/*
		if( algorithmSelection.contains("a") ) {	
			nameComparisonAlgorithm(newJarMethods, errorList);
		}
		
		if( algorithmSelection.contains("b") ) {
			parameterComparisonAlgorithm(newJarMethods, errorList);
		}
		
		if( algorithmSelection.contains("c") ) {
			returnTypeComparisonAlgorithm(newJarMethods, errorList);
		}*/
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
	
	
	private void parameter_returnComparison(List<IMethod> newJarMethods, List<IMethod> oldJarMethods,
			List<String> errorList) throws JavaModelException {
		return_parameterRecommendations.add("----------------------------------------------------------\n");
		return_parameterRecommendations.add("Parameter & Return Type Comparison Recommendations\n");
		return_parameterRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			return_parameterRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			return_parameterRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				if(errorList.get(i).contains("(int")){
					for(String parameter : newJarMethods.get(newJar).getParameterTypes()){
						if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length==1){
							for(int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
								if(oldJarMethods.get(oldJar).getReturnType().equals(newJarMethods.get(newJar).getReturnType())){
									return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses integer as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("I")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses integer as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(errorList.get(i).contains("()")){
					if(newJarMethods.get(newJar).getParameterTypes().length==0){
							return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
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
									return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses String as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("Ljava.lang.String;")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
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
									return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
											"\nis a method that uses double as parameter as well has the same return type, \n"
											+ "this is a possible option to replace "+errorMethodName + " with.\n");
								}
							}
						}
						else if(parameter.equals("D")&& newJarMethods.get(newJar).getParameterTypes().length!=1){
							return_parameterRecommendations.add("The following method: "+ newJarMethods.get(newJar).getElementName()+
									"\nis a method that uses double as parameter as well, but this version only\n"
									+ "handles one argument within the methods.\n");
							break;
						}
					}
				}
				else if(newJar== newJarMethods.size()-1){
					return_parameterRecommendations.add("The current version only handles problem with argument types of: \n"
							+ "void, double, int and String. Sorry for the inconvenience, but I can't support this\n"
							+ "type of problem.\n");
				}
			}
		}
		
	}


	private void name_parameterComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, 
			List<String> errorList) {
		name_parameterRecommendations.add("----------------------------------------------------------\n");
		name_parameterRecommendations.add("Name & Parameter Comparison Recommendations\n");
		name_parameterRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			name_parameterRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			name_parameterRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							if(newJarMethods.get(newJar).getParameterTypes().equals(oldJarMethods.get(oldJar).getParameterTypes())){
								name_parameterRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "As well this method has the same parameter in both versions.\n"
										+ "My guessing is this method has a different return type.\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							else{
								name_parameterRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
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
						name_parameterRecommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						name_parameterRecommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
	}


	private void name_returnComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods,
			List<String> errorList) throws JavaModelException {
		name_returnRecommendations.add("----------------------------------------------------------\n");
		name_returnRecommendations.add("Name & Return Comparison Recommendations\n");
		name_returnRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			name_returnRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			name_returnRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.get(i).contains(oldJarMethods.get(oldJar).getElementName())){
						if(errorList.get(i).contains(newJarMethods.get(newJar).getElementName())){
							if(newJarMethods.get(newJar).getReturnType().equals(oldJarMethods.get(oldJar).getReturnType())){
								name_returnRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
										+ "jar versions and it's generating an error whenever the program compiles.\n"
										+ "As well this method has the same return type in both versions.\n"
										+ "My guessing is this method has different arguments compared to the previous jar version.\n"
										+ "Please be causious and create a new method to implement your new logic, if you are \n"
										+ "not sure how this method behaves.\n");
								newJar= newJarMethods.size();
								oldJar= oldJarMethods.size();
							}
							else{
								name_returnRecommendations.add("The method "+ errorMethodName+ " is currently being implemented in both\n"
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
						name_returnRecommendations.add("The method "+newJarMethods.get(newJar).getElementName() + "is generating a compiling error.\n"
								+ "As well, this method is new considering it didn't exist in the previous jar version. Please make sure\n"
								+ "the return type of the new jar version is the same as your running code, as well the argument types.\n");
					}
					else if(oldJar== oldJarMethods.size()-1 && newJar== newJarMethods.size()-1){
						name_returnRecommendations.add("The generated problem doesn't have this method neither in the previous\n"
								+ "jar version nor in the new jar version. Feel free to create a new method that implements\n"
								+ "what ever logic you wish with this name.\n");
					}
				}
			}
		}
		
	}


	private void name_parameter_returnComaprisonAlgorithm(List<IMethod> newJarMethods, List <IMethod> oldJarMethods, 
			List<String> errorList) throws JavaModelException {
		name_parameter_returnRecommendations.add("----------------------------------------------------------\n");
		name_parameter_returnRecommendations.add("Name, Parameters & Return Comparison Recommendations\n");
		name_parameter_returnRecommendations.add("----------------------------------------------------------\n");
		
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
	}


	/*
	 * Prints recommendations to the textBox in the second pop-up window
	 */
	public void printRecommendations(String algorithmSelection, JTextArea textbox) {
		/*
		if( algorithmSelection.contains("a") ) {
			for(String line : nameRecommendations) {
				MigrationHandler.textBox.append(line);
			}
		}

		if( algorithmSelection.contains("b") ) {
			for(String line : parameterRecommendations) {
				MigrationHandler.textBox.append(line);
			}
		}

		if( algorithmSelection.contains("c") ) {
			for(String line : returnTypeRecommendations) {
				MigrationHandler.textBox.append(line);
			}
		}*/
		
		if(algorithmSelection.contains("a")){
			if(algorithmSelection.contains("b")){
				if(algorithmSelection.contains("c")){
					//message for recommendation based on method's name, parameter type and return type
					for(String line: name_parameter_returnRecommendations){
						textbox.append(line);
					}
				}
				else{
					//message for recommendation based on method's name and parameter type
					for(String line: name_parameterRecommendations){
						textbox.append(line);
					}
				}
			}
			else if(algorithmSelection.contains("c")){
				//message for recommendation based on method's name and return type
				for(String line: name_returnRecommendations){
					textbox.append(line);
				}
			}
			else{
				//message for recommendation based only on method's name
				for(String line: nameRecommendations){
					textbox.append(line);
				}
			}
		}
		else{
			if(algorithmSelection.contains("b")){
				if(algorithmSelection.contains("c")){
					//message for recommendation based on parameter type and return type
					for(String line: return_parameterRecommendations){
						textbox.append(line);
					}
				}
				else{
					//message for recommendation based on parameter type
					for(String line: parameterRecommendations){
						textbox.append(line);
					}
				}
			}
			else if(algorithmSelection.contains("c")){
				//message for recommendation based on return type
				for(String line: returnTypeRecommendations){
					textbox.append(line);
				}
			}
			else{
				//nothing has been chosen!
				textbox.append("You haven't click on any of the check box which will run a set of algorithms, \n"
						+ "that will allow a profound analysis of recommendations for your migration API if you\n"
						+ "have done something innapropietly. Please click on as many options you wish.");
			}			
		}
	}
	
	
	/*
	 * For each IProblem, check if its method name is a substring of a method in the new Jar.
	 * Also do the reverse check, check if the name of jar methods are a substring of the error method name.
	 * Examples: 
	 * 		Will work:
	 * 			Error: printHello 			NewJar: printHelloWorld
	 * 			Error: printGreeting		NewJar: print
	 * 		Won't work:
	 * 			Error: printHello			NewJar: printGreeting
	 * 			Error: printHelloWorld		NewJar: printWorld
	 */
	/*
	public void nameComparisonAlgorithm(List<IMethod> newJarMethods, List<String> errorList) {
		nameRecommendations.add("----------------------------------------------------------\n");
		nameRecommendations.add("Name Comparison Recommendations\n");
		nameRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getErrorMethodName( errorList.get(i) );
			nameRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			nameRecommendations.add("Recommendation(s):\n");
			
			for(int j=0; j < newJarMethods.size(); j++) {
				if( errorMethodName.contains(newJarMethods.get(j).getElementName()) || newJarMethods.get(j).getElementName().contains(errorMethodName) ) {
					nameRecommendations.add( newJarMethods.get(j).getElementName() + "\n" );
				}
			}
		}
	}
	*/
	public void nameComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) {
		nameRecommendations.add("----------------------------------------------------------\n");
		nameRecommendations.add("Name Comparison Recommendations\n");
		nameRecommendations.add("----------------------------------------------------------\n");
		
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
	}	
	/*
	 * 
	 */
	public void parameterComparisonAlgorithm(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList) {
		parameterRecommendations.add("----------------------------------------------------------\n");
		parameterRecommendations.add("Parameter Comparison Recommendations\n");
		parameterRecommendations.add("----------------------------------------------------------\n");
		
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
	}
	
	
	/*
	 * 
	 */
	public void returnTypeComparisonAlgorithm(List<IMethod> newJarMethods, List<String> errorList) {
		returnTypeRecommendations.add("----------------------------------------------------------\n");
		returnTypeRecommendations.add("Return Type Comparison Recommendations\n");
		returnTypeRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getProblemMethodName( errorList.get(i) );
			returnTypeRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			returnTypeRecommendations.add("Recommendation(s):\n");
			returnTypeRecommendations.add("Since IProblem doesn't provide return types, by myself I can't do much.\n"
					+ "Sorry, maybe on the next version or if we ever figure out how to get return types from IProblem\n"
					+ "we would be able to suggest methods that return the same type as the error prompts.\n");
		}
	}
	
	
	/*
	 * Returns the method name given the message of an IProblem
	 * Example:
	 * 		The method setDate(int) from the type Date is deprecated -> setDate
	 *		The method getDate() is undefined for the type MyClass -> getDate
	 */
	static String getProblemMethodName(String errorMessage) {
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
	static String[] getProblemParameters(String message) {
		String[] split = message.split("\\(");
		String temp = split[1];
		split = temp.split("\\)");
		temp = split[0];
		split = temp.split(", ");
		
		if( split.length == 1 && message.contains("()") ) {
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
	static String parseParameter(String parameter) {
		String[] tmp = parameter.split("\\.");
	
		String result = tmp[tmp.length-1];
		result = result.replace(";", "");
	
		return result;
	}
}
