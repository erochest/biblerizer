package com.bibler.biblerizer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.*;


public class GeoReferencePanel extends JPanel {
	
	public JTextArea grTextField;
	JButton buttonBack;
	JButton buttonNext;
	JButton geoReference;
	boolean cleared = false;
	String message = "Please insert bounding coordinates in the following order:\n\n" +
			"North South East West.\n\n" +
			"Or click 'GeoReference' to position your map on the globe.";
	
	ButtonListener listener = new ButtonListener();
	JPanel waitPanel;
	
	public GeoReferencePanel(int width, int height) {
		super();
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.DARK_GRAY);
		grTextField = new JTextArea();
		grTextField.setLineWrap(true);
		grTextField.setWrapStyleWord(true);
		grTextField.setPreferredSize(new Dimension(width - 25, height- 24 * 4));
		grTextField.setFont(BaseObject.sRegistry.messageFont);
		grTextField.setForeground(BaseObject.sRegistry.skyBlueLight);
		grTextField.setBackground(Color.DARK_GRAY);
		grTextField.setText(message);
		grTextField.addMouseListener(new GRPanelMouseListener());
		
		grTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		grTextField.getActionMap().put("enter", new EnterAction());
		grTextField.addKeyListener(new GRKeyListener());
		buttonBack = new JButton("Back");
		buttonNext = new JButton("Next");
		geoReference = new JButton("Geo Reference");
		buttonBack.setFont(BaseObject.sRegistry.messageFont);
		buttonBack.setBackground(Color.DARK_GRAY.brighter());
		buttonBack.setContentAreaFilled(false);
		buttonBack.setOpaque(true);
		buttonBack.setForeground(BaseObject.sRegistry.skyBlue);
		buttonNext.setFont(BaseObject.sRegistry.messageFont);
		buttonNext.setBackground(Color.DARK_GRAY.brighter());
		buttonNext.setContentAreaFilled(false);
		buttonNext.setOpaque(true);
		buttonNext.setForeground(BaseObject.sRegistry.skyBlue);
		geoReference.setFont(BaseObject.sRegistry.messageFont);
		geoReference.setBackground(Color.DARK_GRAY.brighter());
		geoReference.setContentAreaFilled(false);
		geoReference.setOpaque(true);
		geoReference.setForeground(BaseObject.sRegistry.skyBlue);
		buttonBack.setActionCommand("back");
		buttonNext.setActionCommand("next");
		geoReference.setActionCommand("geo");
		buttonNext.addActionListener(listener);
		buttonBack.addActionListener(listener);
		geoReference.addActionListener(listener);
		waitPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(BaseObject.sRegistry.skyBlue);
				int small = 0;
				if(BaseObject.sRegistry.image != null) {
					small = (int) (BaseObject.sRegistry.image.smallifyProgress * 20);
				}
				g.fillRect(0, 0, (int) ((BaseObject.sRegistry.loader.loadProgress * 230)
							+ small), 25);
			}
		};
		waitPanel.setPreferredSize(new Dimension(250, 25));
		waitPanel.setBackground(Color.DARK_GRAY.brighter());
		JLabel label = new JLabel("Image loading, please wait.");
		waitPanel.add(label);
		waitPanel.setVisible(true);
		geoReference.setVisible(false);
		buttonNext.setVisible(false);
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, grTextField, 0, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.NORTH, grTextField, 12, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, buttonNext, -15, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, buttonNext,  -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, buttonBack, 15, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, buttonBack, 0, SpringLayout.NORTH, buttonNext);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, geoReference, 0, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.SOUTH, geoReference, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, waitPanel, 0, SpringLayout.HORIZONTAL_CENTER, geoReference);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, waitPanel, 0, SpringLayout.VERTICAL_CENTER, geoReference);
		setLayout(layout);
		add(grTextField);
		add(buttonBack);
		add(buttonNext);
		add(geoReference);
		add(waitPanel);
	}
	
	public void takeFocus() {
		grTextField.requestFocus();
	}
	
	public void processCoords() {
		String coords = grTextField.getText();
		String[] coordsSplit = coords.split("[ ]");
		int comma;
		float north = 0;
		float south = 0;
		float east = 0;
		float west = 0;
		float angle = 0;
		for(int i = 0; i < coordsSplit.length; i++) {
			switch (i) {
				case 0:
					north = Float.parseFloat(coordsSplit[i]);
					break;
				case 1:
					south = Float.parseFloat(coordsSplit[i]);
					break;
				case 2:
					east = Float.parseFloat(coordsSplit[i]);
					break;
				case 3:
					west = Float.parseFloat(coordsSplit[i]);
					break;
				case 4:
					angle = Float.parseFloat(coordsSplit[i]);
					break;
			}
		}
		BaseObject.sRegistry.controller.setCoords(north, south, east, west, angle);
		setVisible(false);
		BaseObject.sRegistry.mainFrame.outputPanel.setVisible(true);
	}
	
	public boolean checkForGoodness(float north, float south, float east, float west) {
		int val = -9;
		if(north < south) {
			val = JOptionPane.showConfirmDialog(BaseObject.sRegistry.mainFrame, "Your North coordinate is South of your South, which seems odd. Would you like to change it?", "Ummm...", JOptionPane.YES_NO_OPTION);
		
		}
		if(east < west) {
			val = JOptionPane.showConfirmDialog(BaseObject.sRegistry.mainFrame, "Your East coordiate(" + east + ") is West of your West coordiate (" + west + "), which is a bit strange. Would you like to change it?", "Errr...", JOptionPane.YES_NO_OPTION);
		}
		if(north > 180 || south > 180 || east > 180 || west > 180 || north < -180 || south < -180 || east < -180 || west < -180) {
			val = JOptionPane.showConfirmDialog(BaseObject.sRegistry.mainFrame, "One of the coordinates you've entered stretches beyond the limits of Earth, which might have flown with Anaximander, but probably won't work here. Would you like to change it?", "Ahh...", JOptionPane.YES_NO_OPTION);
		}
		if(val == JOptionPane.CANCEL_OPTION || val == JOptionPane.NO_OPTION || val == -9)
			return true;
		else
			return false;
	}
	
	public void reset() {
		grTextField.setText(message);
		cleared = false;
		setVisible(false);
	}
	
	public class GRPanelMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(!cleared) {
				grTextField.setText("");
				cleared = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class EnterAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
				processCoords();
		}
	}
	
	public class GRKeyListener implements KeyListener {

			@Override
			public void keyPressed(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyTyped(KeyEvent e) {
				if(!cleared) {
					grTextField.setText("");
					cleared = true;
				}
			}
	}
	
	public class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("next")) {
				processCoords();
			}
			if(command.equals("back")) {
				goBack();
			}
			if(command.equals("geo")) {
				geo();
			}
		}
	}

	public void geo() {
		setVisible(false);
		BaseObject.sRegistry.grPanel.setVisible(true);
		BaseObject.sRegistry.infoPanel.switchToGeoreference(true);
	}
	
	public void updateButtons(boolean show) {
		waitPanel.setVisible(!show);
		geoReference.setVisible(show);
		buttonNext.setVisible(show);
	}
	
	public void goBack() {
		reset();
		updateButtons(false);
		BaseObject.sRegistry.loadPanel.setVisible(true);
		BaseObject.sRegistry.image = null;
		try {
			BaseObject.sRegistry.loader.decoder.fc.close();
			BaseObject.sRegistry.loader.decoder.tmpFile.close();
		} catch(IOException ex) {}
		finally {
			BaseObject.sRegistry.loader.decoder = null;
			System.gc();
		}
		BaseObject.sRegistry.loader.loadThread.interrupt();
		BaseObject.sRegistry.controller = new Controller();
	}
}
