package com.bibler.biblerizer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;


public class ImageLoadPanel extends JPanel {
	
	JButton loadButton;
	JTextField loadField;
	ButtonListener listener = new ButtonListener();
	
	public ImageLoadPanel(int w, int h) {
		setupLoadPanel(w,h);
	}
	
	public void setupLoadPanel(int width, int height) {
		setTransferHandler(new TransferDrop());
		setPreferredSize(new Dimension(width, height));
		setVisible(true);
		setBackground(Color.DARK_GRAY);

		
		JLabel fileLoadMessage = new JLabel("Choose a map image to begin");
		fileLoadMessage.setFont(BaseObject.sRegistry.messageFont);
		fileLoadMessage.setForeground(BaseObject.sRegistry.skyBlue);
		loadField = new JTextField();
		loadField.setText(FileSystemView.getFileSystemView().getRoots()[0].getAbsolutePath());
		loadField.setCaretPosition(loadField.getText().length());
		loadField.addActionListener(listener);
		loadField.setActionCommand("load_field");
		loadField.setPreferredSize(new Dimension(450, 50));
		loadField.setFont(BaseObject.sRegistry.messageFont);
		loadField.setForeground(BaseObject.sRegistry.skyBlue);
		loadField.setBackground(Color.DARK_GRAY.brighter());
		loadButton = new JButton("  Load  ") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 50);
			}
		};
		loadButton.setEnabled(false);
		loadButton.setActionCommand("load_image");
		loadButton.addActionListener(listener);
		loadButton.setFont(BaseObject.sRegistry.messageFont);
		loadButton.setBackground(Color.DARK_GRAY.brighter());
		loadButton.setContentAreaFilled(false);
		loadButton.setOpaque(true);
		loadButton.setForeground(BaseObject.sRegistry.skyBlue);

		
		SpringLayout layout = new SpringLayout();
		
		layout.putConstraint(SpringLayout.WEST, fileLoadMessage, 12, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, fileLoadMessage, 12, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, loadField, 12, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, loadField, 12, SpringLayout.SOUTH, fileLoadMessage);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, loadButton, 0, SpringLayout.VERTICAL_CENTER, loadField);
		layout.putConstraint(SpringLayout.WEST, loadButton, 5, SpringLayout.EAST, loadField);
		
		this.setLayout(layout);
		this.add(fileLoadMessage);
		this.add(loadButton);
		add(loadField);
	}
	
	public void setImageFile(File f) {
		if(!f.exists() || f.isDirectory()) {
			JOptionPane.showMessageDialog(ImageLoadPanel.this, "Sorry, can't find that file.");
			loadField.setText(FileSystemView.getFileSystemView().getRoots()[0].getAbsolutePath());
			return;
		}
		String s = f.getAbsolutePath().substring(f.getAbsolutePath().length() - 3);
		if(!s.equalsIgnoreCase("png")) {
			JOptionPane.showMessageDialog(ImageLoadPanel.this, "This doesn't seem to be a PNG.\nWe need a PNG.\nI'm sorry.");
			loadField.setText(FileSystemView.getFileSystemView().getRoots()[0].getAbsolutePath());
		}
		BaseObject.sRegistry.controller.setFile(f);
		BaseObject.sRegistry.imageFileLocation = f;
		BaseObject.sRegistry.fileRoot = f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - 4);
		BaseObject.sRegistry.outputPanel.outputField.setText(f.getParent());
		
		ImageLoadPanel.this.setVisible(false);
		BaseObject.sRegistry.geoReferencePanel.setVisible(true);
		BaseObject.sRegistry.geoReferencePanel.takeFocus();
	}
	
	public class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("load_image")) {
				int returnVal = BaseObject.sRegistry.chooser.showOpenDialog(BaseObject.sRegistry.mainFrame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File f = BaseObject.sRegistry.chooser.getSelectedFile();
					setImageFile(f);
				}
			} else if(command.equals("load_field")) {
				File f = new File(loadField.getText().trim());
				setImageFile(f);
			}
		}
	}
	
	public class TransferDrop extends TransferHandler {
		
		private boolean isReadableByImageIO(DataFlavor flavor) {
	        Iterator<?> readers = ImageIO.getImageReadersByMIMEType(
	            flavor.getMimeType());
	        if (readers.hasNext()) {
	            Class<?> cls = flavor.getRepresentationClass();
	            return (InputStream.class.isAssignableFrom(cls) ||
	                    URL.class.isAssignableFrom(cls) ||
	                    File.class.isAssignableFrom(cls));
	        }

	        return false;
	    }

	    @Override
	    public boolean canImport(TransferSupport support) {
	        if (support.getUserDropAction() == LINK) {
	            return false;
	        }

	        for (DataFlavor flavor : support.getDataFlavors()) {
	            if (flavor.equals(DataFlavor.javaFileListFlavor)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    @Override
	    public boolean importData(TransferSupport support) {
	        if (!canImport(support)) {
	            return false;
	        }

	        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            try {
	                Iterable<?> list = (Iterable<?>)
	                    support.getTransferable().getTransferData(
	                        DataFlavor.javaFileListFlavor);
	                Iterator<?> files = list.iterator();
	                if (files.hasNext()) {
	                    File file = (File) files.next();
	                    setImageFile(file);
	                    return true;
	                }
	            } catch (UnsupportedFlavorException | IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return false;
	    }
	}

}
