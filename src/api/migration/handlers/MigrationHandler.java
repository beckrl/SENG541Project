package api.migration.handlers;

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;

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
	private JTextArea textBox;
	
	
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
		selectionFrame = new JFrame();
		
		selectionFrame.setBounds(150, 150, 450, 300);
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
		projectChoice.setBounds(200, 45, 200, 30);
		selectionFrame.getContentPane().add(projectChoice);
		
		//Add list of projects to dropdown menu
		for (IProject project : workspaceProjects) {
			projectChoice.add(project.getName());
		}
						
		// Old jar selection dropdown menu
		oldJarChoice = new Choice();
		oldJarChoice.setBounds(200, 95, 200, 30);
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
	        	System.out.println("Old jar path: " + oldJarPath);
	        }
	    });
	    	    
		// Selecting a new jar file button
	    selectNewJarButton = new JButton("Select Jar File");
		selectNewJarButton.setBounds(235, 145, 120, 30);
		selectionFrame.getContentPane().add(selectNewJarButton);
		selectNewJarButton.addActionListener(this);
	    
		// Migration button
		migrateButton = new JButton("Migrate");
		migrateButton.setBounds(150, 210, 150, 30);
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
	        	System.out.println("New jar path: " + newJarPath);
				try {
					processRootDirectoryJar();
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
		}
		
		// Migration Button
		if (e.getSource() == migrateButton) {
			if ( oldJarChoice.getItemCount() == 1 ) {
				oldJarPath = oldJarChoice.getItem(0);
				System.out.println(oldJarPath);

			}
			
			if (oldJarPath != null && newJarPath != null) {
				// Clone the selected project as ProjectName-new 
				try { cloneSelectedProject(selectedProject.getLocation().toString()); }
				catch (CoreException | FileNotFoundException e1) { e1.printStackTrace(); }
							
				// Draw the second window showing compilations errors and prompting user for recommendation algorithm
				drawRecommendationWindow();
				
				try { analyseMethods(clonedProject); } 
				catch (JavaModelException e1) { e1.printStackTrace(); }
				
				selectionFrame.setVisible(false);
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
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();

		frame.setBounds(100, 100, 600, 400);
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
				
		JButton btnRecommendation = new JButton("Recommendation");
		btnRecommendation.setBounds(100, 320, 159, 29);
		panel.add(btnRecommendation);
		
		JRadioButton rdbtnAlgorithm = new JRadioButton("Algorithm 1");
		rdbtnAlgorithm.setBounds(319, 310, 107, 23);
		panel.add(rdbtnAlgorithm);
		
		JRadioButton radioButton = new JRadioButton("Algorithm 2");
		radioButton.setBounds(319, 330, 107, 23);
		panel.add(radioButton);
		
		frame.setVisible(true);
	}

	
	/*
	 * Analyse the method calls in project
	 */
	private void analyseMethods(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		// parse(JavaCore.create(project));
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				createAST(mypackage);
			}
		}
	}
	
	
	private void getJarMethods() {
		CompilationUnit unit;
		
		
	}

	
	/*
	 * Create AST
	 */
	private void createAST(IPackageFragment mypackage) throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			// now create the AST for the ICompilationUnits
			CompilationUnit compunit = parse(unit);
		
			IProblem[] problems = compunit.getProblems();
			for (IProblem problem : problems) {
				System.out.println(problem);
			}
			
			MethodVisitor visitor = new MethodVisitor();
			compunit.accept(visitor);

			for (MethodDeclaration method : visitor.getMethods()) {
				textBox.append("Method name: " + method.getName() + "  Return type: " + method.getReturnType2() + "\n");
			}
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
	
	
	/* The following methods are from the online tutorial - don't know if we'll need them
	private void printProjectInfo(IProject project) throws CoreException, JavaModelException {
		System.out.println("Working in project " + project.getFullPath());
		// check if we have a Java project
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			IJavaProject javaProject = JavaCore.create(project);
			printPackageInfos(javaProject);
		}
	}

	private void printPackageInfos(IJavaProject javaProject) throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			// Package fragments include all packages in the classpath
			// We will only look at the package from the source folder
			// K_BINARY would include also included JARS, e.g. rt.jar
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				System.out.println("Package " + mypackage.getElementName());
				printICompilationUnitInfo(mypackage);
			}
		}
	}
	
	private void printICompilationUnitInfo(IPackageFragment mypackage) throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			printCompilationUnitDetails(unit);
		}
	}

	private void printIMethods(ICompilationUnit unit) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		for (IType type : allTypes) {
			printIMethodDetails(type);
		}
	}

	private void printCompilationUnitDetails(ICompilationUnit unit) throws JavaModelException {
		System.out.println("Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());
		System.out.println("Has number of lines: " + doc.getNumberOfLines());
		printIMethods(unit);
	}

	private void printIMethodDetails(IType type) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			System.out.println("Method name " + method.getElementName());
			System.out.println("Signature " + method.getSignature());
			System.out.println("Return Type " + method.getReturnType());
		}
	}
	*/
	
	/*Sonny's attend to retrieve jar files within a project
	 Code retrieved from: http://www.programcreek.com/2012/06/traverse-jar-file-by-using-eclipse-jdt/
	 */
	private void processRootDirectoryJar() throws JavaModelException,CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		System.out.println("root" + root.getLocation().toOSString());
	 
		IProject[] projects = root.getProjects();
	 
		// process each project
		for (IProject project : projects) {
	 
			System.out.println("project name: " + project.getName());
	 
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
	 
				// process each package
				for (IPackageFragment aPackage : packages) {
	 
					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS, e.g. rt.jar
					// only process the JAR files
					if (aPackage.getKind() == IPackageFragmentRoot.K_BINARY
							&& aPackage.getElementName().equals("java.lang")) {
	 
						System.out.println("inside of java.lang package");
	 
						for (IClassFile classFile : aPackage.getClassFiles()) {
	 
							System.out.println("----classFile: "
									+ classFile.getElementName());
	 
							// A class file has a single child of type IType.
							// Class file elements need to be opened before they
							// can be navigated. If a class file cannot be
							// parsed, its structure remains unknown.
							// Use IJavaElement.isStructureKnown to determine
							// whether this is the case.
	 
							// System.out.println();
							// classFile.open(null);
	 
							for (IJavaElement javaElement : classFile
									.getChildren()) {
	 
								if (javaElement instanceof IType) {
									System.out.println("--------IType "
											+ javaElement.getElementName());
	 
									// IInitializer
									IInitializer[] inits = ((IType) javaElement)
											.getInitializers();
									for (IInitializer init : inits) {
										System.out
												.println("----------------initializer: "
														+ init.getElementName());
									}
	 
									// IField
									IField[] fields = ((IType) javaElement)
											.getFields();
									for (IField field : fields) {
										System.out
												.println("----------------field: "
														+ field.getElementName());
									}
	 
									// IMethod
									IMethod[] methods = ((IType) javaElement)
											.getMethods();
									for (IMethod method : methods) {
										System.out
												.println("----------------method: "
														+ method.getElementName());
										System.out
												.println("----------------method return type - "
														+ method.getReturnType());
									}
								}
							}
						}
	 
					}
				}
	 
			}
	 
		}
	}
}