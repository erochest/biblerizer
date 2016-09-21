package com.bibler.biblerizer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;


public class OutputPanel extends JPanel {
	
	JButton output;
	JTextField outputField;
	JTextArea outputMessage;
	JLabel processingLabel;
	JButton tile;
	JPanel centralPanel;
	int width;
	int height;
	
	ButtonListener listener = new ButtonListener();
	
	public OutputPanel(int w, int h) {
		super();
		width = w;
		height = h;
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.DARK_GRAY);
		setup();
	}
	
	public void setup() {
		outputMessage = new JTextArea("Confirm the directory where you'd " +
				"like the tiles output.");
		outputMessage.setFont(BaseObject.sRegistry.messageFont);
		outputMessage.setForeground(BaseObject.sRegistry.skyBlue);
		outputMessage.setBackground(Color.DARK_GRAY);
		outputMessage.setLineWrap(true);
		outputMessage.setWrapStyleWord(true);
		outputMessage.setEditable(false);
		outputMessage.setPreferredSize(new Dimension(width, 50));
		output = new JButton("Change") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 50);
			}
		};
		output.setActionCommand("change");
		output.addActionListener(listener);
		outputField = new JTextField();
		outputField.setFont(BaseObject.sRegistry.messageFont);
		outputField.setForeground(BaseObject.sRegistry.skyBlue);
		outputField.setBackground(Color.DARK_GRAY.brighter());
		outputField.setPreferredSize(new Dimension(450, 50));
		
		tile = new JButton("Tile");
		tile.setEnabled(false);
		output.setFont(BaseObject.sRegistry.messageFont);
		output.setBackground(Color.DARK_GRAY.brighter());
		output.setContentAreaFilled(false);
		output.setOpaque(true);
		output.setForeground(BaseObject.sRegistry.skyBlue);
		tile.setFont(BaseObject.sRegistry.messageFont);
		tile.setBackground(Color.DARK_GRAY.brighter());
		tile.setContentAreaFilled(false);
		tile.setOpaque(true);
		tile.setForeground(BaseObject.sRegistry.skyBlue);
		tile.setActionCommand("tile");
		tile.addActionListener(listener);
		tile.setEnabled(true);
		tile.setVisible(true);
		processingLabel = new JLabel("Processing   ");
		processingLabel.setVisible(false);
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.WEST, outputField, 12, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, outputField, 12, SpringLayout.SOUTH, outputMessage);
		layout.putConstraint(SpringLayout.WEST, output, 5, SpringLayout.EAST, outputField);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, output, 0, SpringLayout.VERTICAL_CENTER, outputField);
		layout.putConstraint(SpringLayout.WEST, outputMessage, 12, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, outputMessage, 12,  SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, tile, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, tile, -12, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, processingLabel, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, processingLabel, -12, SpringLayout.EAST, this);

		
		setLayout(layout);
		add(outputMessage);
		add(outputField);
		add(output);
		add(tile);
		add(processingLabel);
	}
	
	public void reset() {
		setVisible(false);
	}

	public void startTiling() {
		tile.setVisible(false);
		processingLabel.setVisible(true);
		BaseObject.sRegistry.progressPanel.start();
		BaseObject.sRegistry.controller.start();
		Runnable r = new Runnable() {
			long lastTime = System.currentTimeMillis();
			int processingEllipses = 0;

			@Override
			public void run() {
				while(!(BaseObject.sRegistry.controller.started && BaseObject.sRegistry.progressPanel.tilePanel.loaded)) {
					if(System.currentTimeMillis() - lastTime >= 333) {
						lastTime = System.currentTimeMillis();
						processingEllipses++;
						if(processingEllipses > 4) {
							processingEllipses = 0;
						}
						String ellipses = "";
						for(int i = 0; i < 4; i++) {
							if(i < processingEllipses)
								ellipses += ".";
							else
								ellipses += " ";
						}
						processingLabel.setText("Processing" + ellipses);
					}
				}
				OutputPanel.this.setVisible(false);
				BaseObject.sRegistry.progressPanel.setVisible(true);

			}

		};
		Thread t = new Thread(r);
		t.start();
	}
	
	public class ButtonListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("change")) {
				int returnVal = BaseObject.sRegistry.chooser.showSaveDialog(BaseObject.sRegistry.mainFrame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File f = BaseObject.sRegistry.chooser.getSelectedFile();
					BaseObject.sRegistry.fileRoot = f.getAbsolutePath();
					outputField.setText(f.getAbsolutePath());
				}
			}
			if(command.equals("tile")) {
				boolean tile = true;
				if(new File(BaseObject.sRegistry.fileRoot).exists()) {
					Object[] options = {"Yes, please",
							"No, thanks"};
					int n = JOptionPane.showOptionDialog(OutputPanel.this,
							"Output directory already exists,\n would you like to overwrite?",
							"Whoopsie",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[1]);
					if(n == 0) {
						deleteDirectory(new File(BaseObject.sRegistry.fileRoot));
						tile = true;
					} else {
						tile = false;
					}

				}
				if(tile) {
					startTiling();

				}
			}
		}
	}

	public static boolean deleteDirectory(File directory) {
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
		return(directory.delete());
	}

}
