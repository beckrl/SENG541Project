package api.migration.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;

public class Recommender {

	// Class Variables
	private List<IMethod> changedMethods = new ArrayList<IMethod>();
	private List<String> nameRecommendations = new ArrayList<String>();
	private List<String> parameterRecommendations = new ArrayList<String>();
	private List<String> returnTypeRecommendations = new ArrayList<String>();
	
	
	/*
	 * Constructor
	 */
	public Recommender(List<IMethod> oldJarMethods, List<IMethod> newJarMethods, List<String> errorList) {
		
		// Compare all methods in new Jar with old Jar.
		// Any methods that are new or have been modified since the old Jar are added to changedMethods list.
		for(int i=0; i < newJarMethods.size(); i++) {
			Boolean found = false;
			for(int j=0; j < oldJarMethods.size(); j++) {
				if( newJarMethods.get(i).getElementName().equals( oldJarMethods.get(j).getElementName() ) ) {
					found = true;
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
	public void executeAlgorithms(List<IMethod> newJarMethods, List<String> errorList, String algorithmSelection) {
		if( algorithmSelection.contains("a") ) {	
			nameComparisonAlgorithm(newJarMethods, errorList);
		}
		
		if( algorithmSelection.contains("b") ) {
			parameterComparisonAlgorithm(newJarMethods, errorList);
		}
		
		if( algorithmSelection.contains("c") ) {
			returnTypeComparisonAlgorithm(newJarMethods, errorList);
		}
	}
	
	
	/*
	 * Prints recommendations to the textBox in the second pop-up window
	 */
	public void printRecommendations(String algorithmSelection) {
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
	
	
	/*
	 * 
	 */
	public void parameterComparisonAlgorithm(List<IMethod> newJarMethods, List<String> errorList) {
		parameterRecommendations.add("----------------------------------------------------------\n");
		parameterRecommendations.add("Parameter Comparison Recommendations\n");
		parameterRecommendations.add("----------------------------------------------------------\n");
		
		for(int i=0; i < errorList.size(); i++) {
			String errorMethodName = getErrorMethodName( errorList.get(i) );
			parameterRecommendations.add("\nError Method Name: " + errorMethodName + "\n");
			parameterRecommendations.add("Recommendation(s):\n");
			
			/////////////////////////////////////////////////
			// FOR EACH ERROR - DO SOMETHING LOGICAL HERE
			/////////////////////////////////////////////////
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
