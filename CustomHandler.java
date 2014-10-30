package testplugin.handlers;

import java.awt.Choice;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class CustomHandler extends AbstractHandler {
	/**
	 * The constructor.
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSelectjarTo = new JLabel("Select existing .jar files:");
		lblSelectjarTo.setBounds(28, 73, 163, 16);
		frame.getContentPane().add(lblSelectjarTo);
		
		JLabel lblSelectjarTo_1 = new JLabel("Select replacement .jar files:");
		lblSelectjarTo_1.setBounds(28, 145, 163, 16);
		frame.getContentPane().add(lblSelectjarTo_1);
		
		JButton btnMigrate = new JButton("Migrate");
		btnMigrate.setBounds(203, 203, 117, 29);
		frame.getContentPane().add(btnMigrate);
		
		Choice choice = new Choice();
		choice.setBounds(203, 62, 125, 27);
		frame.getContentPane().add(choice);
		
		textField = new JTextField();
		textField.setBounds(203, 133, 163, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		frame.setVisible(true);
	}
}
