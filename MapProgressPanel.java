package com.bibler.biblerizer;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.*;


public class MapProgressPanel extends JPanel {
	
	ProgressBar progressBar;
	JLabel timeRemainingLabel;
	public MapTileProgressPanel tilePanel;
	
	int tilesFinished = 0;
	long startTime;
	int lastSeconds;
	
	
	public MapProgressPanel(int width, int height) {
		super();
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.DARK_GRAY);
		tilePanel = new MapTileProgressPanel();
		BaseObject.sRegistry.tilePanel = tilePanel;
		progressBar = new ProgressBar(512, 24);
		timeRemainingLabel = new JLabel("Time Remaining: -:--");
		timeRemainingLabel.setFont(BaseObject.sRegistry.messageFontSmall);
		timeRemainingLabel.setForeground(Color.WHITE);

		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.NORTH, progressBar, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, progressBar, 0, SpringLayout.WEST, tilePanel);
		layout.putConstraint(SpringLayout.NORTH, tilePanel, 0, SpringLayout.SOUTH, progressBar);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tilePanel, 0, SpringLayout.HORIZONTAL_CENTER, this);
		setLayout(layout);
		add(progressBar);
		add(tilePanel);
		layout = new SpringLayout();
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, timeRemainingLabel, 0, SpringLayout.VERTICAL_CENTER, progressBar);
		progressBar.setLayout(layout);
		progressBar.add(timeRemainingLabel);
		timeRemainingLabel.setText("Writing tiles...");
		timeRemainingLabel.setVisible(false);
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		//timeRemainingLabel.setVisible(true);
		lastSeconds = 0;
	}
	
	public void done() {
		int current = (int) (System.currentTimeMillis() - startTime) / 1000;
		int minutes = (int) Math.floor(current / 60);
		int seconds = (int) (current % 60);
		int minutesTens = minutes / 10;
		int minutesOnes = minutes % 10;
		int secondsTens = seconds / 10;
		int secondsOnes = seconds % 10;
		Object[] options = {"Preview Tiles",
				"Show in Folder",
				"Exit"};
		int n = JOptionPane.showOptionDialog(MapProgressPanel.this,
				"Done in " + minutesTens + minutesOnes + ":" + secondsTens + secondsOnes + "!\nClick Preview to see result (opens default browser).",
				"Done!", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		String c = "";
		switch(n) {
			case 0:
				c = "PREVIEW";
				break;
			case 1:
				c = "SHOW";
				break;
			case 2:
				BaseObject.sRegistry.controller.exit();
		}
		finish(c);
	}
	
	public void updateTime() {
		if(1 == 1)
			return;
		int current = (int) (System.currentTimeMillis() - startTime) / 1000;
		int minutes = (int) Math.floor(current / 60);
		int seconds = (int) (current % 60);
		int minutesTens = minutes / 10;
		int minutesOnes = minutes % 10;
		int secondsTens = seconds / 10;
		int secondsOnes = seconds % 10;
		if(seconds == lastSeconds)
			return;
		lastSeconds = seconds;
		int timeRemaining = (int) ((current / ((BaseObject.sRegistry.pixelsWrit / (float) BaseObject.sRegistry.totalPixels)))
				+ (BaseObject.sRegistry.assumedWriteTime + BaseObject.sRegistry.assumedReadTime) / 1000);
		timeRemaining -= current;
		minutes = (int) Math.floor(timeRemaining / 60);
		seconds = (int) (timeRemaining % 60);
		minutesTens = minutes / 10;
		minutesOnes = minutes % 10;
		secondsTens = seconds / 10;
		secondsOnes = seconds % 10;
		if(BaseObject.sRegistry.writingTiles) {
			timeRemainingLabel.setText("Writing Tiles...");
		} else
			timeRemainingLabel.setText("Time Remaining: " + minutesTens + minutesOnes + ":" + secondsTens + secondsOnes);

	}
	
	public void updateProgress(float percent) {
		progressBar.setValue(percent);
	}
	
	public void incrementProgress() {
		updateTime();
		progressBar.setValue(BaseObject.sRegistry.pixelsWrit / (float) BaseObject.sRegistry.totalPixels);
		progressBar.repaint();
	}
	
	public void reset() {
		progressBar.setValue(0);
		timeRemainingLabel.setVisible(false);
		timeRemainingLabel.setText("Time Remaining: --:--");
		setVisible(false);
	}

	public void finish(String command) {
		if(!Desktop.isDesktopSupported())
			return;
		try {
			if(command.equals("PREVIEW"))
				Desktop.getDesktop().browse(HTMLGenerator.createTestSite());
			else if(command.equals("SHOW"))
				Desktop.getDesktop().open(new File(BaseObject.sRegistry.fileRoot));
		} catch(IOException ex) {}

	}
	
	public class ProgressBar extends JPanel {
		float progress;
		int height;
		int width;
		
		public ProgressBar(int w, int h) {
			super();
			width = w;
			height = h;
			setPreferredSize(new Dimension(w,h));
			setBackground(Color.DARK_GRAY);

		}
		
		public void setValue(float p) {
			progress = p;
			repaint();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(progress >= 1) {
				return;
			}
			g.setColor(BaseObject.sRegistry.skyBlue);
			g.fillRect(0, 0, (int) (width * progress), height);
			
		}
	}

}
