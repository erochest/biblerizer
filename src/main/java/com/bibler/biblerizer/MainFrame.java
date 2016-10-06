package com.bibler.biblerizer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class MainFrame extends JFrame {
	
	JPanel mainPanel;
	InfoPanel infoPanel;
	ImageLoadPanel imageLoadPanel;
	GeoReferencePanel geoReferencePanel;
	GRPanel grPanel;
	OutputPanel outputPanel;
	MapProgressPanel progressPanel;
	
	JFileChooser chooser;
	
	int width;
	int height;
	
	int mainPanelWidth;
	int mainPanelHeight;
	
	Thread chooserThread;

	JMenuBar menuBar;
	
	public MainFrame() {
		super("Biblerizer - V2.3787563");
		URL imageURL = MainFrame.class.getResource("Images/icon.png");
		if (imageURL != null) {
			Image img = null;
			try {
				img = ImageIO.read(imageURL);
			} catch (IOException e) {}
			this.setIconImage(img);
		}
		BaseObject.sRegistry.mainFrame = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupMenu();
		width = 576;
		height = 600;
		mainPanelWidth = width;
		mainPanelHeight = 512 + 24;
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));
		infoPanel = new InfoPanel(width, height - 48 - 512);
		BaseObject.sRegistry.infoPanel = infoPanel;
		imageLoadPanel = new ImageLoadPanel(mainPanelWidth, mainPanelHeight);
		BaseObject.sRegistry.loadPanel = imageLoadPanel;
		setupFileChooser();
		
		geoReferencePanel = new GeoReferencePanel(mainPanelWidth, mainPanelHeight);
		geoReferencePanel.setVisible(false);
		BaseObject.sRegistry.geoReferencePanel = geoReferencePanel;
		outputPanel = new OutputPanel(mainPanelWidth, mainPanelHeight);
		outputPanel.setVisible(false);
		BaseObject.sRegistry.outputPanel = outputPanel;
		progressPanel = new MapProgressPanel(mainPanelWidth, mainPanelHeight);
		progressPanel.setVisible(false);
		BaseObject.sRegistry.progressPanel = progressPanel;
		grPanel = new GRPanel();
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, imageLoadPanel, 0, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, imageLoadPanel, 0, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, geoReferencePanel, 0, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, grPanel, 0, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, grPanel, 0, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, geoReferencePanel, 0, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, outputPanel, 0, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, outputPanel, 0, SpringLayout.NORTH, mainPanel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, progressPanel, 0, SpringLayout.HORIZONTAL_CENTER, mainPanel);
		layout.putConstraint(SpringLayout.NORTH, progressPanel, 0, SpringLayout.NORTH, mainPanel);
		mainPanel.setLayout(layout);
		mainPanel.add(grPanel);
		mainPanel.add(imageLoadPanel);
		mainPanel.add(outputPanel);
		mainPanel.add(geoReferencePanel);
		mainPanel.add(progressPanel);
		grPanel.setVisible(false);
		layout = new SpringLayout();
		JPanel panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setPreferredSize(new Dimension(width, height - 24));
		layout.putConstraint(SpringLayout.NORTH, mainPanel, 0, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.SOUTH, infoPanel, 0, SpringLayout.SOUTH, panel);
		panel.setLayout(layout);
		panel.add(infoPanel);
		panel.add(mainPanel);
		add(panel);
		pack();
	}

	public void setupMenu() {
		MenuListener listener = new MenuListener();
		menuBar = new JMenuBar();
		menuBar.setBackground(Color.DARK_GRAY);
		JMenu file = new JMenu("File");
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(listener);
		exit.setActionCommand("EXIT");
		file.add(exit);
		menuBar.add(file);
		setJMenuBar(menuBar);
	}
	
	public void resetAll() {
		progressPanel.reset();
		outputPanel.reset();
		geoReferencePanel.reset();
		imageLoadPanel.setVisible(true);
		BaseObject.sRegistry.controller = new Controller();
		BaseObject.sRegistry.pixelsWrit = 0;
		BaseObject.sRegistry.infoPanel.reset();
		BaseObject.sRegistry.progressPanel.reset();
	}
	
	public void setupFileChooser() {
		chooserThread = new Thread(new Runnable() {

			@Override
			public void run() {
				chooser = new JFileChooser();
			
				chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File arg0) {
						if(arg0.isDirectory())
							return true;
						String suffix = arg0.getAbsolutePath().substring(arg0.getAbsolutePath().length() - 3);
						return (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("png"));
					}

					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
				});
				BaseObject.sRegistry.chooser = chooser;
				BaseObject.sRegistry.loadPanel.loadButton.setEnabled(true);
				boolean retry = true;
				while(retry) {
					try {
						chooserThread.join();
						retry = false;
					} catch(InterruptedException e) {}
				}
			}
			
		});
		chooserThread.start();
	}
	
	public class MenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("EXIT")) {
				if(BaseObject.sRegistry.image != null) {
					BaseObject.sRegistry.image.disposeImage();
				}
				System.exit(0);
			}
		}
	}
}
