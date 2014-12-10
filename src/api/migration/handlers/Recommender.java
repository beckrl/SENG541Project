package api.migration.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.eclipse.jdt.core.IMethod;

public class Recommender {

	// Class Variables
	private List<IMethod> changedMethods;
	
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
	public void executeAlgorithms(List<IMethod> newJarMethods, List<IMethod> oldJarMethods, List<String> errorList, String algorithmSelection) {
		nameRecommendations = new ArrayList<String>();
		parameterRecommendations = new ArrayList<String>();
		returnTypeRecommendations = new ArrayList<String>();
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
					name_parameter_returnComaprisonAlgorithm(newJarMethods, errorList);
				}
				else{
					//checking for errors by analyzing methods name and their parameters type
					name_parameterComparisonAlgorithm(newJarMethods, errorList);
				}
				
			}
			else if (algorithmSelection.contains("c")){
				//checking for errors by analyzing methods name and their return type
				name_returnComparisonAlgorithm(newJarMethods, errorList);
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
					parameter_returnComparison(newJarMethods, errorList);
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
	
	
	private void parameter_returnComparison(List<IMethod> newJarMethods,
			List<String> errorList) {
		// TODO Auto-generated method stub
		
	}


	private void name_parameterComparisonAlgorithm(List<IMethod> newJarMethods,
			List<String> errorList) {
		// TODO Auto-generated method stub
		
	}


	private void name_returnComparisonAlgorithm(List<IMethod> newJarMethods,
			List<String> errorList) {
		// TODO Auto-generated method stub
		
	}


	private void name_parameter_returnComaprisonAlgorithm(
			List<IMethod> newJarMethods, List<String> errorList) {
		// TODO Auto-generated method stub
		
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
			String errorMethodName = getErrorMethodName( errorList.get(i) );
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
			String errorMethodName = getErrorMethodName( errorList.get(i) );
			nameRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			nameRecommendations.add("Recommendation(s):\n");
			
			for(int newJar=0; newJar < newJarMethods.size(); newJar++) {
				for( int oldJar=0; oldJar<oldJarMethods.size(); oldJar++){
					if(errorList.contains("int")){
						
					}
					else if(errorList.contains("()")){
						
					}
					else if(errorList.contains("String")){
						
					}
					else if(errorList.contains("double")){
						
					}
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
			String errorMethodName = getErrorMethodName( errorList.get(i) );
			returnTypeRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			returnTypeRecommendations.add("Recommendation(s):\n");
			
			/////////////////////////////////////////////////
			// FOR EACH ERROR - DO SOMETHING LOGICAL HERE
			/////////////////////////////////////////////////
		}
	}
	
	
	/*
	 * Returns the method name given the message of an IProblem
	 * Example:
	 * 		The method setDate(int) from the type Date is deprecated -> setDate
	 *		The method getDate() is undefined for the type MyClass -> getDate
	 */
	static String getErrorMethodName(String errorMessage) {
		String [] parse = errorMessage.split("\\s+");
		String temp = "";
		
		for(int i = 2; i < parse.length; i++)
			temp += parse[i] + " ";
	
		parse = temp.split("\\)");
		
		return parse[0].split("\\(")[0];
	}
}
