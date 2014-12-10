package api.migration.handlers;

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import api.migration.MethodVisitor;


public class MigrationHandler extends AbstractHandler implements ActionListener{
	
	/* Class Variables */
	private IProject[] workspaceProjects;
	private IProject selectedProject;
	private IProject clonedProject;
	
	private String workspacePath;
	private String oldJarPath;
	private String newJarPath;
	
	private Choice projectChoice;
	private Choice oldJarChoice;
	
	private JFrame selectionFrame;
	private JButton selectNewJarButton;
	private JButton migrateButton;
	public static JTextArea textBox;
	
	private List<String> errorList;
	private List<IMethod> oldJarMethods;
	private List<IMethod> newJarMethods;
	
	// Variables for the algorithm selection
	static public int alg1selection;
	static public int alg2selection;
	static public int alg3selection;
	static public String algorithmSelection; 
	
	
	/* 
	 * Executes when "API Migration" button is clicked on context menu 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Access the current Eclipse workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    
	    // Saves the full path of the workspace
	    workspacePath = root.getLocation().toString();
	    
	    // Get all projects in the workspace and store them in IProject[] projects
	    workspaceProjects = root.getProjects();
	    	    	    
		drawFrame();

	    return null;
	}
	
	
	/* 
	 * Draw the pop-up window when API Migration button is clicked. 
	 */
	private void drawFrame() {
		// Setting up the frame
		selectionFrame = new JFrame("API Migrator");
		
		selectionFrame.setBounds(150, 150, 700, 300);
		selectionFrame.getContentPane().setLayout(null);
		
		// Setting up labels
		JLabel lblSelectProject = new JLabel("Select your project:");
		lblSelectProject.setBounds(25, 50, 163, 16);
		selectionFrame.getContentPane().add(lblSelectProject);
		
		JLabel lblSelectOldJar = new JLabel("Select existing .jar file:");
		lblSelectOldJar.setBounds(25, 100, 163, 16);
		selectionFrame.getContentPane().add(lblSelectOldJar);
		
		JLabel lblSelectNewJar = new JLabel("Select replacement .jar files:");
		lblSelectNewJar.setBounds(25, 150, 163, 16);
		selectionFrame.getContentPane().add(lblSelectNewJar);
		
		// Project selection dropdown menu
		projectChoice = new Choice();
		projectChoice.removeAll();
		projectChoice.setBounds(200, 45, 400, 30);
		selectionFrame.getContentPane().add(projectChoice);
		
		//Add list of projects to dropdown menu
		for (IProject project : workspaceProjects) {
			projectChoice.add(project.getName());
		}
						
		// Old jar selection dropdown menu
		oldJarChoice = new Choice();
		oldJarChoice.setBounds(200, 95, 400, 30);
		selectionFrame.getContentPane().add(oldJarChoice);
		
		// Listener for when a project is selected
	    projectChoice.addItemListener( new ItemListener() {
	        public void itemStateChanged(ItemEvent ie)
	        {
	        	selectedProject = null;
	        	String projectName = projectChoice.getSelectedItem();
	        	
	        	// Get the IProject that matches the user selection from the dropdown menu
	        	for (IProject project : workspaceProjects) {
	        		if (project.getName() == projectName) {
	        			selectedProject = project;
	        		}
	        	}
	        	
	        	if (selectedProject != null) {	        		
	        		// Access the build path of the project
	        		IJavaProject javaProject = JavaCore.create(selectedProject);
	        		IClasspathEntry[] entries = null;
					
	        		try {
						entries = javaProject.getRawClasspath();
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
	        		
	        		oldJarChoice.removeAll();

	        		// From the classpath list, add only the libraries to the second dropdown menu
	        		for (IClasspathEntry entry : entries) {
	        			if (entry.toString().contains("CPE_LIBRARY")) {
	        				String jarFileName = entry.toString().replaceAll("[.]jar.*", ".jar");
	        				oldJarChoice.add(jarFileName);
	        				oldJarPath = oldJarChoice.getSelectedItem();
	        			}
	        		}
	        	}
	        }
	    });
	    
	    // Listener for when an old jar file is selected
	    oldJarChoice.addItemListener( new ItemListener() {
	        public void itemStateChanged(ItemEvent ie)
	        {
	        	oldJarPath = oldJarChoice.getSelectedItem();
	        	System.out.println("The old jar path is: " + oldJarPath);
	        }
	    });
	    	    
		// Selecting a new jar file button
	    selectNewJarButton = new JButton("Select Jar File");
		selectNewJarButton.setBounds(200, 145, 150, 30);
		selectionFrame.getContentPane().add(selectNewJarButton);
		selectNewJarButton.addActionListener(this);
	    
		// Migration button
		migrateButton = new JButton("Migrate");
		migrateButton.setBounds(200, 210, 150, 30);
		selectionFrame.getContentPane().add(migrateButton);
		migrateButton.addActionListener(this);
			    				
	    // Make the frame visible
		selectionFrame.setVisible(true);
	}
	
	
	/* 
	 * Responses to actions in the first pop-up window
	 */
	public void actionPerformed(ActionEvent e) {
		// Button that opens window for selecting new jar file
		if (e.getSource() == selectNewJarButton) {
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Jar Files", "jar");
			fileChooser.setFileFilter(filter);
			
	        int returnValue = fileChooser.showOpenDialog(null);
	        if (returnValue == JFileChooser.APPROVE_OPTION) {
	        	File selectedFile = fileChooser.getSelectedFile();
	        	newJarPath = selectedFile.getPath();
	        	System.out.println("The new jar path is: " + newJarPath);
	        }
		}
		
		// Migration Button
		if (e.getSource() == migrateButton) {
			if ( oldJarChoice.getItemCount() == 1 ) {
				oldJarPath = oldJarChoice.getItem(0);
			}
			
			if (oldJarPath != null && newJarPath != null) {
				// Clone the selected project as ProjectName-new 
				try { cloneSelectedProject(selectedProject.getLocation().toString()); }
				catch (CoreException | FileNotFoundException e1) { e1.printStackTrace(); }
							
				// Draw the second window showing compilations errors and prompting user for recommendation algorithm
				drawRecommendationWindow();
				
				selectionFrame.setVisible(false);
				
				// Get method details of old JAR file
				oldJarMethods = new ArrayList<IMethod>();
				try { analyseJarMethods(selectedProject, oldJarPath); }
				catch (JavaModelException e1) { e1.printStackTrace(); }

				// Get method details of new JAR file
				newJarMethods = new ArrayList<IMethod>();
				try { analyseJarMethods(clonedProject, newJarPath); }
				catch (JavaModelException e1) { e1.printStackTrace(); }
				
				// Get the compilation issues from the new project
				errorList = new ArrayList<String>();
				try { analyseMethods(clonedProject); } 
				catch (JavaModelException e1) { e1.printStackTrace(); }
				
				printInfoToConsole();
			}
		}		
	}
	
	
	/* 
	 * Creates a duplicate of the selectedProject with the new jar file
	 */
	public void cloneSelectedProject(String projectPath) throws CoreException, FileNotFoundException {		
		File srcDir = new File(projectPath);
		File destDir = new File(projectPath + "-new");
		
		if ( !srcDir.exists() ) {
			throw new FileNotFoundException("Source project not found");
		}
		
		// Duplicate the selected project as ProjectName-new in the workspace
		try { FileUtils.copyDirectory(srcDir, destDir); }
		catch (IOException e) { e.printStackTrace(); }
		
		// Get the project name from the project's description
		IProjectDescription description;
		description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projectPath + "-new/.project"));
		String projectName = description.getName();

		// Replace the Project Name in the description of the cloned project
		try {
			String content = FileUtils.readFileToString(new File(projectPath + "/.project"));
			content = content.replaceAll(projectName, projectName+"-new");
			File tempFile = new File(projectPath + "-new/.project");
			FileUtils.writeStringToFile(tempFile, content);
		} catch (IOException e) { e.printStackTrace(); }
		
		// Update the cloned project description and save the project as IProject clonedProject
		// Import the project into the Eclipse package explorer
		description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projectPath + "-new/.project"));
		clonedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		
		Boolean alreadyCopied = false;
		for (IProject entry : workspaceProjects) {
			if (clonedProject == entry) {
				alreadyCopied = true;
			}
		}
		
		if(alreadyCopied == false) {
			clonedProject.create(description, null);
			clonedProject.open(null);
		}
		
		changeClasspath(clonedProject);
	}
	
		
	/*
	 * Modifies the classpath of a project
	 */
	private void changeClasspath(IProject project) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		
		for (int i = 0; i < entries.length; i++) {
			if ( oldJarPath.replace("\\", "/").matches( entries[i].getPath().toString() )) {
				IClasspathEntry newJarEntry = JavaCore.newLibraryEntry(new Path(newJarPath), null, null);
				entries[i] = newJarEntry;
				javaProject.setRawClasspath(entries, null);
			}
		}
	}
	

	/* 
	 * Draws the second pop-up window after Migrate button has be clicked
	 */ 
	public void drawRecommendationWindow() {
		JFrame frame = new JFrame("API Migrator- Recommendations");
		JPanel panel = new JPanel();
		algorithmSelection="";
		alg1selection=0;
		alg2selection=0;
		alg3selection=0;
	
		frame.setBounds(100, 100, 600, 425);
		frame.setContentPane(panel);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(null);
		
		// Temporary close on exit to make it easier to test
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		textBox = new JTextArea();
		textBox.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textBox, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(30, 30, 540, 270);
		panel.add(scrollPane);
				
		JCheckBox checkboxAlg1 = new JCheckBox("JAR File Comparison");
		checkboxAlg1.setBounds(319, 310, 200, 23);
		checkboxAlg1.addActionListener(new ActionListener() {
			@Override
	        public void actionPerformed(ActionEvent e) {
				alg1selection++;
				if(alg1selection%2!=0){	
					algorithmSelection+="a";
				}
				else{
					if(algorithmSelection.contains("a")){
						algorithmSelection= algorithmSelection.replace("a","");
					}
				}
			}
	    });
		panel.add(checkboxAlg1);
		
		JCheckBox checkboxAlg2 = new JCheckBox("Parameter Comparison");
		checkboxAlg2.setBounds(319, 330, 200, 23);
		checkboxAlg2.addActionListener(new ActionListener() {
			@Override
	        public void actionPerformed(ActionEvent e) {
				alg2selection++;
				if(alg2selection%2!=0){
					algorithmSelection+="b";
				}
				else{
					if(algorithmSelection.contains("b")){
						algorithmSelection= algorithmSelection.replace("b","");
					}
				}
			}
	    });
		panel.add(checkboxAlg2);
		
		JCheckBox checkboxAlg3 = new JCheckBox("Return Type Comparison");
		checkboxAlg3.setBounds(319, 350, 200, 23);
		checkboxAlg3.addActionListener(new ActionListener() {
			@Override
	        public void actionPerformed(ActionEvent e) {
				alg3selection++;
				if(alg3selection%2!=0){
					algorithmSelection+="c";
				}
				else{
					if(algorithmSelection.contains("c")){
						algorithmSelection= algorithmSelection.replace("c","");
					}
				}
			}
	    });
		panel.add(checkboxAlg3);
		
		JButton btnRecommendation = new JButton("Recommendation");
		btnRecommendation.setBounds(100, 325, 159, 29);
		btnRecommendation.addActionListener(new ActionListener(){
			@Override
			// Executes when Recommendation button is clicked
		    public void actionPerformed(ActionEvent e) {
				// Clear textBox
				textBox.setText("");
				
				// Invokes the recommender class
				Recommender recommender = new Recommender(oldJarMethods, newJarMethods, errorList);
				recommender.executeAlgorithms(newJarMethods, oldJarMethods, errorList, algorithmSelection);
				recommender.printRecommendations(algorithmSelection, textBox);
				
				// Move the caret of the textbox back to the top once everything is printed
				textBox.setCaretPosition(0);
			}	
		});
		
		panel.add(btnRecommendation);
		frame.setVisible(true);
	}

	
	/*
	 * Analyse the method calls in Jar file
	 */
	private void analyseJarMethods(IProject project, String jarPath) throws JavaModelException {
		IPath path = new Path(jarPath);
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for ( IPackageFragment mypackage : packages ) {
			if ( mypackage.getKind() == IPackageFragmentRoot.K_BINARY ) {
				for ( IClassFile classFile : mypackage.getClassFiles() ) {
					if( classFile.getPath().toString().equals(path.toString()) ) {
						for ( IJavaElement javaElement : classFile.getChildren() ) {
							if (javaElement instanceof IType) {								
								// IMethod
								IMethod[] methods = ((IType) javaElement).getMethods();
								
								for( IMethod method : methods) {
									if(jarPath == oldJarPath) {
										if(method.isConstructor() == false)
											oldJarMethods.add( method );
									}
									else {
										if(method.isConstructor() == false)
										newJarMethods.add( method );
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	/*
	 * Analyse the method calls in project
	 */
	private void analyseMethods(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				createAST(mypackage);
			}
		}
	}

	
	/*
	 * Create AST
	 */
	private void createAST(IPackageFragment mypackage) throws JavaModelException {		
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			// now create the AST for the ICompilationUnits
			CompilationUnit compunit = parse(unit);
		
			IProblem[] problems = compunit.getProblems();
			
			for( IProblem problem : problems ) {
				errorList.add( problem.getMessage() );
			}
			//MethodVisitor visitor = new MethodVisitor();
			//compunit.accept(visitor);
		}
	}
	
	
	/* 
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java source file
	 */ 
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
	
	
	/*
	 * Prints useful information to the Eclipse console
	 */
	private void printInfoToConsole() {
		// Print the method details of the old jar file
		try {
			System.out.println("\nThe old jar has this many methods: " + oldJarMethods.size());
			for (IMethod method : oldJarMethods) {
				System.out.println( "Name:       " + method.getElementName() );
				System.out.println( "ReturnType: " + method.getReturnType() );
				System.out.print( "Parameters: " );
				if(method.getNumberOfParameters() > 0) {
					for(String parameter : method.getParameterTypes())
						System.out.print( parameter + ", " );
				}
				else { System.out.print( "None" ); }
				System.out.print( "\n\n" );
			}
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		// Print the method details of the new jar file
		try {
			System.out.println("\nThe new jar has this many methods: " + newJarMethods.size());
			for (IMethod method : newJarMethods) {
				System.out.println( "Name:       " + method.getElementName() );
				System.out.println( "ReturnType: " + method.getReturnType() );
				System.out.print( "Parameters: " );
				if(method.getNumberOfParameters() > 0) {
					for(String parameter : method.getParameterTypes())
						System.out.print( parameter + ", " );
				}
				else { System.out.print( "None" ); }
				System.out.print( "\n\n" );
			}
		} catch (JavaModelException e) { e.printStackTrace(); }
		
		// Print the list of problems in the new project
		System.out.println("\nThe list of IProblems in the new project:");
		for(String error : errorList) { 
			System.out.println(error); 
		}
	}
}	// end of program tag
