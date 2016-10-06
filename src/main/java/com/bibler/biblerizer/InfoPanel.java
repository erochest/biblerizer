package com.bibler.biblerizer;
//import javafx.scene.control.Slider;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;


public class InfoPanel extends JPanel {
	
	JLabel imageNameLabel;
	JLabel fileLocationLabel;
	JLabel imageWidthLabel;
	JLabel northLabel;
	JLabel southLabel;
	JLabel eastLabel;
	JLabel westLabel;
	JTextField northField;
	JTextField southField;
	JTextField eastField;
	JTextField westField;
	JSlider rotationSlider;
	JSlider transparencySlider;
	JTextField rotationField;
	JLabel rotationLabel;
	int width;
	int height;
	boolean valueChangedFromMap;
	
	public InfoPanel(int w, int h) {
		super();
		width = w;
		height = h;
		setPreferredSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setBackground(Color.darkGray.brighter());
		imageNameLabel = new JLabel("Title: ");
		fileLocationLabel = new JLabel("Location: ");
		imageWidthLabel = new JLabel("Dims: ");
		northLabel = new JLabel("North: ");
		southLabel = new JLabel("South: ");
		eastLabel = new JLabel("East: ");
		westLabel = new JLabel("West: ");
		northField = new JTextField();
		southField = new JTextField();
		eastField = new JTextField();
		westField = new JTextField();
		Dimension nsewDim = new Dimension((int) ((width * .86666f) - (width * .66666f)) - 35, 20);
		northField.setPreferredSize(nsewDim);
		southField.setPreferredSize(nsewDim);
		eastField.setPreferredSize(nsewDim);
		westField.setPreferredSize(nsewDim);
		northField.setText(" - - ");
		southField.setText(" - - ");
		eastField.setText("   - - ");
		westField.setText("   - - ");
		northField.setOpaque(false);
		southField.setOpaque(false);
		eastField.setOpaque(false);
		westField.setOpaque(false);
		northField.setBorder(null);
		southField.setBorder(null);
		eastField.setBorder(null);
		westField.setBorder(null);
		CoordListener coordListener = new CoordListener();
		northField.addActionListener(coordListener);
		southField.addActionListener(coordListener);
		eastField.addActionListener(coordListener);
		westField.addActionListener(coordListener);
		rotationLabel = new JLabel("Rot: ");
		rotationField = new JTextField();
		rotationField.setText("0.0");
		rotationField.setOpaque(false);
		rotationField.setBorder(null);
		rotationField.setPreferredSize(new Dimension(50, 10));
		SliderListener listener = new SliderListener();
		rotationSlider = new JSlider(JSlider.HORIZONTAL, -360, 360, 0);
		rotationSlider.setUI(new CustomSliderUI(rotationSlider, InfoPanel.class.getClassLoader().getResourceAsStream("images/slider_thumb.png")));
		rotationSlider.setOpaque(false);
		rotationSlider.setBorder(null);
		rotationSlider.setPreferredSize(new Dimension(200, 10));
		rotationSlider.addChangeListener(listener);
		transparencySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		transparencySlider.setUI(new CustomSliderUI(transparencySlider, InfoPanel.class.getClassLoader().getResourceAsStream("images/slider_thumb.png")));
		transparencySlider.setOpaque(false);
		transparencySlider.setBorder(null);
		transparencySlider.setPreferredSize(new Dimension(200, 20));
		transparencySlider.addChangeListener(listener);
		switchToGeoreference(false);
		setup();
	}
	
	public void setup() {
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.WEST, rotationSlider, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, rotationSlider, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, rotationLabel, 3, SpringLayout.EAST, rotationSlider);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, rotationLabel, 0, SpringLayout.VERTICAL_CENTER, rotationSlider);
		layout.putConstraint(SpringLayout.WEST, rotationField, 0, SpringLayout.EAST, rotationLabel);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, rotationField, 0, SpringLayout.VERTICAL_CENTER, rotationLabel);
		layout.putConstraint(SpringLayout.WEST, transparencySlider, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, transparencySlider, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST,  imageNameLabel,     5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, imageNameLabel,     5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST,  fileLocationLabel,  5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, fileLocationLabel,  5, SpringLayout.SOUTH, imageNameLabel);
		layout.putConstraint(SpringLayout.WEST,  imageWidthLabel,  width / 3, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, imageWidthLabel,  5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, northLabel,  (int) (width * .6f), SpringLayout.WEST,  this);
		layout.putConstraint(SpringLayout.NORTH, northLabel,  5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, southLabel,  0, SpringLayout.WEST, northLabel);
		layout.putConstraint(SpringLayout.NORTH, southLabel,  0, SpringLayout.NORTH, fileLocationLabel);
		layout.putConstraint(SpringLayout.WEST, eastLabel,   (int) (width * .8f), SpringLayout.WEST,  this);
		layout.putConstraint(SpringLayout.NORTH, eastLabel,   5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, westLabel,   0, SpringLayout.WEST, eastLabel);
		layout.putConstraint(SpringLayout.NORTH, westLabel,   0, SpringLayout.NORTH, fileLocationLabel);

		layout.putConstraint(SpringLayout.WEST, northField,  0, SpringLayout.EAST,  northLabel);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, northField,  0, SpringLayout.VERTICAL_CENTER, northLabel);
		layout.putConstraint(SpringLayout.WEST, southField,  0, SpringLayout.WEST, northField);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, southField,  0, SpringLayout.VERTICAL_CENTER, southLabel);
		layout.putConstraint(SpringLayout.WEST, eastField,   0, SpringLayout.EAST,  eastLabel);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, eastField,   0, SpringLayout.VERTICAL_CENTER, eastLabel);
		layout.putConstraint(SpringLayout.WEST, westField,   0, SpringLayout.WEST, eastField);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, westField,   0, SpringLayout.VERTICAL_CENTER, westLabel);
		
		setLayout(layout);
		add(rotationField);
		add(rotationLabel);
		add(rotationSlider);
		add(transparencySlider);
		add(imageNameLabel);
		add(fileLocationLabel);
		add(imageWidthLabel);
		add(northLabel);
		add(southLabel);
		add(eastLabel);
		add(westLabel);
		add(northField);
		add(southField);
		add(eastField);
		add(westField);
	}

	public void setRotation(double theta) {
		valueChangedFromMap = true;
		rotationSlider.setValue((int) theta);
		BigDecimal bd = new BigDecimal(Double.toString(theta));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		rotationField.setText(" " + bd);
		valueChangedFromMap = false;
	}

	public void switchToGeoreference(boolean on) {
		imageNameLabel.setVisible(!on);
		fileLocationLabel.setVisible(!on);
		imageWidthLabel.setVisible(!on);
		rotationField.setVisible(on);
		rotationLabel.setVisible(on);
		rotationSlider.setVisible(on);
		transparencySlider.setVisible(on);
	}
	
	public void updateImageInfo(int w, int h, File f) {
		String name = f.getName();
		name = name.substring(0, name.length() - 4);
		imageNameLabel.setText("Image: " + name);
		fileLocationLabel.setText("Location: " + f.getAbsolutePath());
		imageWidthLabel.setText("Dims: " + w + "," + h);
		invalidate();
	}
	
	public void updateCoords(float n, float s, float e, float w) {
		northField.setText(" " + n);
		southField.setText(" " + s);
		eastField.setText(" " + e);
		westField.setText(" " + w);
		invalidate();
	}
	
	public void reset() {
		imageNameLabel.setText("Title: ");
		fileLocationLabel.setText("Location: ");
		imageWidthLabel.setText("Dims: ");
		northField.setText("North - -");
		southField.setText("South - -");
		eastField.setText("East - -");
		westField.setText("West - -");
	}

	public class CustomSliderUI extends BasicSliderUI {

		private BasicStroke stroke = new BasicStroke(3f);

		private Image thumbImage;

		public CustomSliderUI(JSlider b, InputStream stream) {
			super(b);
			System.out.println(stream);
			try {
				thumbImage = ImageIO.read(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g, c);
		}

		@Override
		protected Dimension getThumbSize() {
			return new Dimension(10, 10);
		}

		@Override
		public void paintTrack(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			Stroke old = g2d.getStroke();
			g2d.setStroke(stroke);
			g2d.setPaint(Color.DARK_GRAY);
			if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
				g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2,
						trackRect.x + trackRect.width, trackRect.y + trackRect.height / 2);
			}
			g2d.setStroke(old);
		}

		@Override
		public void paintThumb(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			int x = thumbRect.x;
			int y = thumbRect.y;
			g2d.drawImage(thumbImage, x, y, null);
		}
	}

	public class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			float value = source.getValue();
			if(source == rotationSlider) {
				if(valueChangedFromMap)
					return;
				BaseObject.sRegistry.grPanel.rect.rotate(Math.toRadians(value), true);
				BigDecimal bd = new BigDecimal(Float.toString(value));
				bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				rotationField.setText(" " + bd);
			} else {
				value /= 100.0f;
				BaseObject.sRegistry.grPanel.alpha = value;
				BaseObject.sRegistry.grPanel.repaint();
			}
		}
	}

	public class CoordListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg) {
			float n = Float.parseFloat(northField.getText());
			float s = Float.parseFloat(southField.getText());
			float e = Float.parseFloat(eastField.getText());
			float w = Float.parseFloat(westField.getText());
			BaseObject.sRegistry.grPanel.rect.moveToCoords(n, s, e, w);
		}
	}

}
