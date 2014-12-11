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
	public Recommender(List<IMethod> oldJarMethods, List<String> errorList, JTextArea textBox) {
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
	public void executeAlgorithms(List<IMethod> newJarMethods, String algorithmSelection) throws JavaModelException {
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
					name_parameter_returnComaprisonAlgorithm(newJarMethods);
				}
				else{
					//checking for errors by analyzing methods name and their parameters type
					name_parameterComparisonAlgorithm(newJarMethods);
				}
				
			}
			else if (algorithmSelection.contains("c")){
				//checking for errors by analyzing methods name and their return type
				name_returnComparisonAlgorithm(newJarMethods);
			}
			else{
				//checking for errors by analyzing only methods name
				nameComparisonAlgorithm(newJarMethods);
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
					parameter_returnComparison(newJarMethods);
				}
				else{
					//checking for errors by analyzing methods by their parameter type
					parameterComparisonAlgorithm(newJarMethods);
				}
			}
			else if(algorithmSelection.contains("c")){
				//checking errors for only return types
				returnTypeComparisonAlgorithm(newJarMethods);
			}
			else{
				//nothing has been chosen, it will display nothing has been chosen!
			}
		}
	}

	
	/*
	 * If both parameter and return type comparison heuristic is selected
	 */
	private void parameter_returnComparison(List<IMethod> newJarMethods) throws JavaModelException {
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
	}


	/*
	 * If both name and parameter comparison heuristic is selected
	 */
	private void name_parameterComparisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
	}


	/*
	 * If both name and return type comparison heuristic is selected
	 */
	private void name_returnComparisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
	}


	/*
	 * If all heuristic is selected
	 */
	private void name_parameter_returnComaprisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
	}

	
	/*
	 * If only name comparison heuristic is selected
	 */
	private void nameComparisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
	}
	
	
	/*
	 * If only parameter comparison heuristic is selected
	 */
	private void parameterComparisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
	}
	
	
	/*
	 * If only return type comparison heuristic is selected
	 */
	private void returnTypeComparisonAlgorithm(List<IMethod> newJarMethods) throws JavaModelException {
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
			e.printStackTrace();
		} catch (Exception e) {
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
				e.printStackTrace();
			}
		}
		else {
			for(String arg : args) {
				try {
					result += parseType(arg) + ",";
				} catch (Exception e) {
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
