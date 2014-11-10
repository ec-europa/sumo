/**
 * 
 */
package org.fenggui.util.fonttoolkit;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.fenggui.render.Font;
import org.fenggui.util.Alphabet;

/**
 * @author Rainer Angermann
 *
 */
@SuppressWarnings("serial")
public class FengFonts extends JDialog implements ActionListener 
{
	private JComboBox fontCombo; 
	private DefaultComboBoxModel fontComboModel;
	private JCheckBox antialiasedCheck;
	private JCheckBox boldCheck;
	private JCheckBox italicCheck;
	private JTextField sizeField;
	//private JButton colorButton;
	private JButton generateButton;
	private JButton quitButton;
	
	public FengFonts() {
		initializeComponents();
		
		this.setTitle("FengFonts");
		this.pack();
		this.setVisible(true);
	}
	
	private void initializeComponents() {
		fontComboModel = new DefaultComboBoxModel();
		fontCombo = new JComboBox(fontComboModel);
		antialiasedCheck = new JCheckBox("Anti-aliasing");
		boldCheck = new JCheckBox("Bold");
		italicCheck = new JCheckBox("Italic");
		sizeField = new JTextField("10");
		//colorButton = new JButton("..");
		generateButton = new JButton("Generate...");
		generateButton.addActionListener(this);
		quitButton = new JButton("Quit");
		quitButton.addActionListener(this);
		
		loadFonts();
		
		this.setLayout(new GridLayout(6, 2));
		this.add(new JLabel("Font"));
		this.add(fontCombo);
		this.add(new JLabel());
		this.add(antialiasedCheck);
		this.add(new JLabel());
		this.add(boldCheck);
		this.add(new JLabel());
		this.add(italicCheck);
		this.add(new JLabel("Size"));
		this.add(sizeField);
		//this.add(new JLabel("Color"));
		//this.add(colorButton);
		this.add(generateButton);
		this.add(quitButton);
	}

	/**
	 * Loads all available fonts into the combo
	 *
	 */
	private void loadFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		
		for(String font : fontNames) {
			fontComboModel.addElement(font);
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused") 
		FengFonts fc = new FengFonts();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(quitButton)) {
			System.exit(0);
		} else if(e.getSource().equals(generateButton)) {
			generateFont();
		}
	}

	/**
	 * Generates the font with the given settings
	 *
	 */
	private void generateFont() {
		if(!checkValues())
			return;
		
		String path = getSavePath();
		if(path == null)
			return;
		
		String fontName = (String)fontCombo.getSelectedItem();
		int style = 0;
		if(boldCheck.isSelected())
			style += java.awt.Font.BOLD;
		if(italicCheck.isSelected())
			style += java.awt.Font.ITALIC;
			
		int size = Integer.parseInt(sizeField.getText());
		boolean antialias = antialiasedCheck.isSelected();
			
		java.awt.Font awtFont = new java.awt.Font(fontName, style, size);
		Font font = FontFactory.renderStandardFont(awtFont, antialias, Alphabet.getDefaultAlphabet());
		
		try {
			font.writeFontData(path + ".png", path + ".font");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return Returns false if there is an invalid value in the form
	 */
	private boolean checkValues() {
		int size = 0;
		try {
			size = Integer.parseInt(sizeField.getText());
		} catch(NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid size", "FengFonts", JOptionPane.OK_OPTION);
			return false;
		}
		if(size < 1)
			return false;
		
		return true;
	}

	/**
	 * @return Returns the save path or null if the user doesn't want to save
	 */
	private String getSavePath() {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		
		// restrict to *.png files
		FileFilter ff = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".png") 
		          || f.isDirectory();
			}
			
			public String getDescription() {
		        return "PNG";
		      }
		};
		
		chooser.setFileFilter(ff);
		
		File chosenFile = null;
		int option = chooser.showSaveDialog(this);
		if(option == JFileChooser.APPROVE_OPTION) {
			chosenFile = chooser.getSelectedFile();
		} else if(option == JFileChooser.CANCEL_OPTION) {
			return null;
		}
		
		if(chosenFile!=null&&chosenFile.canRead()) {
			int opt = JOptionPane.showConfirmDialog(this, 
					"Overwrite existing file?", 
					"FontCreator",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			
			if(opt == JOptionPane.CANCEL_OPTION) {
				return null;
			}
			if(opt == JOptionPane.NO_OPTION) {
				return null;
			}
		}
		
		if(chosenFile != null) {
			return chosenFile.getAbsolutePath();
		}
		else
			return null;
	}
}
