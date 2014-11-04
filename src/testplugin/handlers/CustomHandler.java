package testplugin.handlers;

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class CustomHandler extends AbstractHandler implements ActionListener{
	private JButton btnMigrate;
	private JButton btnSelectJar;
	
	/**
	 * The constructor.
	 * @wbp.parser.entryPoint
	 */
	public CustomHandler() {
	}

	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		drawFrame();
		
		return null;
	}
	
	private void drawFrame(){
		JFrame frame = new JFrame();
		JTextField textField;
		
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSelectjarTo = new JLabel("Select existing .jar files:");
		lblSelectjarTo.setBounds(28, 73, 163, 16);
		frame.getContentPane().add(lblSelectjarTo);
		
		JLabel lblSelectjarTo_1 = new JLabel("Select replacement .jar files:");
		lblSelectjarTo_1.setBounds(28, 145, 163, 16);
		frame.getContentPane().add(lblSelectjarTo_1);
		
		btnSelectJar = new JButton("Select .Jar to replace with");
		btnSelectJar.setBounds(203, 133, 200, 30);
		frame.getContentPane().add(btnSelectJar);
		btnSelectJar.addActionListener(this);
	
		btnMigrate = new JButton("Immigrate");
		btnMigrate.setBounds(frame.getWidth()/2, 203, 117, 30);
		frame.getContentPane().add(btnMigrate);
		btnMigrate.addActionListener(this);
		
		Choice choice = new Choice();
		choice.setBounds(203, 62, 200, 30);
		File jarFolder= new File("C:\\Program Files\\Java\\jre1.8.0_20\\lib");
		File[] listOfFiles= jarFolder.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File jarFolder, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
			
		});
		
/*
		for(int i=0; i<listOfFiles.length; i++){
			if(listOfFiles[i].isFile()){
				String file= listOfFiles[i].getName();
				choice.add(file);	
			}
		}
*/
		frame.getContentPane().add(choice);
	
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		final JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter= new FileNameExtensionFilter("Jar Files", "jar");
		fc.setFileFilter(filter);
		
		if(e.getSource() == btnSelectJar){
			fc.showDialog(btnSelectJar, "Select");
			
			String tmp = fc.getSelectedFile().getAbsolutePath();
			System.out.println(tmp);
		}
		
		if(e.getSource()== btnMigrate){
			//do the migration magic!
			System.out.println("migration is happening!");
		}
	}
}
