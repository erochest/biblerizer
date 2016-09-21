package com.bibler.biblerizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Ryan on 1/31/2015.
 */
public class GRPanel extends JPanel {

    BufferedImage img;
    Map map;
    JButton okButton;
    JButton cancelButton;

    int imageWidth;
    int imageHeight;
    int curWidth;
    int curHeight;
    int panelWidth = 512;
    int panelHeight = 536;
    int mapWidth = 512;
    int mapHeight = 488;
    int currentClick = -1;
    float alpha = 1;
    float angle;
    Rectangle zoomControlRect;
    Image zoomControlImg;
    ImageIcon road;
    ImageIcon globe;
    JButton mapTypeButton;
    Coordinate nw = new Coordinate();

    AffineTransform transform = new AffineTransform();
    Color[] colors;

    RotateRect rect;
    RotateRect initialRect;
    int lastX;
    int lastY;

    public GRPanel() {
        map = new Map(-10, 10, 4, mapWidth, mapHeight);
        BaseObject.sRegistry.map = map;
        BaseObject.sRegistry.grPanel = this;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.DARK_GRAY);
        addMouseListener(new Listener());
        addMouseMotionListener(new MotionListener());
        colors = new Color[4];
        colors[0] = Color.BLACK;
        colors[1] = Color.CYAN;
        colors[2] = Color.YELLOW;
        colors[3] = Color.GREEN;
        java.net.URL url;
        try {
            url = GRPanel.class.getClassLoader().getResource("images/zoom_control.png");
            zoomControlImg = ImageIO.read(url);
            url = GRPanel.class.getClassLoader().getResource("images/globe.png");
            globe = new ImageIcon(ImageIO.read(url));
            url = GRPanel.class.getClassLoader().getResource("images/road.png");
            road = new ImageIcon(ImageIO.read(url));
        } catch(IOException e) {}
        zoomControlRect = new Rectangle(12, 12, 24, 48);
        mapTypeButton = new JButton();
        mapTypeButton.setIcon(road);
        mapTypeButton.setOpaque(false);
        mapTypeButton.setContentAreaFilled(false);
        GRButtonListener listener = new GRButtonListener();
        mapTypeButton.setActionCommand("map_type");
        mapTypeButton.addActionListener(listener);

        okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(listener);
        okButton.setFont(BaseObject.sRegistry.messageFont);
        okButton.setBackground(Color.DARK_GRAY.brighter());
        okButton.setContentAreaFilled(false);
        okButton.setOpaque(true);
        okButton.setForeground(BaseObject.sRegistry.skyBlue);
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(listener);
        cancelButton.setFont(BaseObject.sRegistry.messageFont);
        cancelButton.setBackground(Color.DARK_GRAY.brighter());
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(true);
        cancelButton.setForeground(BaseObject.sRegistry.skyBlue);
        SpringLayout layout = new SpringLayout();
        layout.putConstraint(SpringLayout.WEST, cancelButton, 24, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, okButton, -24, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, okButton, -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.NORTH, mapTypeButton, 12, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, mapTypeButton, -12, SpringLayout.EAST, this);
        setLayout(layout);
        add(okButton);
        add(cancelButton);
        add(mapTypeButton);
    }

    public void setImage(BufferedImage i) {
        img = i;
        imageWidth = img.getWidth();
        imageHeight = img.getHeight();
        curWidth = (int) (panelWidth * .25f);
        curHeight = (int) (imageHeight * (curWidth / (float) imageWidth));
        rect = new RotateRect(256 - (curWidth / 2), 256 - (curHeight / 2), curWidth, curHeight, imageWidth, imageHeight);
        rect.centerInDegrees = new Coordinate(-10, 10);
        initialRect = new RotateRect(256 - (curWidth / 2), 256 - (curHeight / 2), curWidth, curHeight, imageWidth, imageHeight);
        initialRect.centerInDegrees = new Coordinate(-10, 10);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setClip(0, 0, mapWidth, mapHeight);
        map.drawMap(g);
        if(img == null)
            return;
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        ((Graphics2D) g).setComposite(ac);
        transform.setToIdentity();
        transform.translate(rect.center.x, rect.center.y);
        transform.rotate(rect.rotation);
        transform.translate(-rect.center.x, -rect.center.y);
        transform.translate(rect.center.x - (rect.rotateWidth / 2), rect.center.y - (rect.rotateHeight / 2));
        transform.scale(rect.scaleX, rect.scaleY);
        ((Graphics2D)g).drawImage(img, transform, null);
        ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
        ((Graphics2D) g).setComposite(ac);
        if(BaseObject.sRegistry.drawLines) {
            g.setColor(Color.RED);
            Vector2D r;
            Vector2D p;
            int x;
            int y;
            int cX = (int) rect.center.x;
            int cY = (int) rect.center.y;
            for (int i = 0; i < rect.corners.length; i++) {
                r = rect.corners[i];
                p = rect.corners[(i + 3) % 4];
                g.setColor(colors[i]);
                x = (int) (r.x < cX ? r.x - 10 : r.x);
                y = (int) (r.y < cY ? r.y - 10 : r.y);
                g.fillOval(x, y, 10, 10);
                g.setColor(Color.BLUE);
                g.drawLine((int) r.x, (int) r.y, (int) p.x, (int) p.y);
            }
            g.setColor(Color.RED);
            g.fillOval((int) rect.center.x, (int) rect.center.y, 10, 10);
            g.fillOval((int) rect.rotateTab.x, (int) rect.rotateTab.y, 10, 10);
            g.drawLine((int) rect.rotateTab.x + 5, (int) rect.rotateTab.y + 5, (int) rect.center.x + 5, (int) rect.center.y + 5);
            g.setColor(Color.YELLOW);
            g.drawRect((int) rect.bbX, (int) rect.bbY, (int) rect.bbWidth, (int) rect.bbHeight);
        }
        g.drawImage(zoomControlImg, zoomControlRect.x, zoomControlRect.y, null);
        g.setClip(0, 0, panelWidth, panelHeight);
    }
    
    public void goBack() {
    	setVisible(false);
    	BaseObject.sRegistry.geoReferencePanel.reset();
    	BaseObject.sRegistry.geoReferencePanel.updateButtons(true);
    	BaseObject.sRegistry.geoReferencePanel.setVisible(true);
    	rect = initialRect.clone();
    	rect.centerInDegrees = new Coordinate(-10, 10);
    	repaint();
    }


    public class Listener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if(zoomControlRect.contains(x, y)) {
                if(y > zoomControlRect.y + (zoomControlRect.height / 2)) {
                    map.zoom(-1);
                } else {
                    map.zoom(1);
                }
            } else if(e.getClickCount() == 2) {
                Vector2D delta = map.goToPoint(new Vector2D(x,y));
                rect.center.x -= delta.x;
                rect.center.y -= delta.y;
                rect.moveAllCorners(-delta.x, -delta.y);
                map.zoom(1);
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            lastX = x;
            lastY = y;
            Rectangle r = new Rectangle(0, 0, 10, 10);
            Vector2D p;
            int cX = (int) rect.center.x;
            int cY = (int) rect.center.y;
            for(int i = 0; i < rect.corners.length; i++) {
                p = rect.corners[i];
                r.x = (int) (p.x < cX ? p.x - 10 : p.x);
                r.y = (int) (p.y < cY ? p.y - 10 : p.y);
                if(r.contains(x, y)) {
                    currentClick = i;
                }
            }
            r.x = (int) rect.center.x;
            r.y = (int) rect.center.y;
            if(r.contains(x, y)) {
                currentClick = 5;
            }
            r.x = (int) rect.rotateTab.x;
            r.y = (int) rect.rotateTab.y;
            if(r.contains(x, y)) {
                currentClick = 6;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            currentClick = -1;
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public class MotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if(currentClick < 0 && !map.updating) {
                int deltaX = e.getX() - lastX;
                int deltaY = e.getY() - lastY;
                Vector2D mapDelta = map.translate(new Vector2D(-deltaX, deltaY));
                rect.center.x -= mapDelta.x;
                rect.center.y -= mapDelta.y;
                rect.moveAllCorners(-mapDelta.x, -mapDelta.y);

            }else if(currentClick < 5)
                rect.moveAVector2D(currentClick, new Vector2D(x,y));
            else if(currentClick == 5) {
                float diffX = x - rect.center.x;
                float diffY = y - rect.center.y;
                rect.center.x = x;
                rect.center.y = y;
                rect.centerInDegrees.lon = MathUtils.pixelToLon(rect.center.x + map.nwPixels.lon, map.curZoom);
                rect.centerInDegrees.lat = MathUtils.pixelToLat(rect.center.y + map.nwPixels.lat, map.curZoom);
                rect.moveAllCorners(diffX, diffY);

            } else if(currentClick == 6) {
                rect.rotateFromDrag(new Vector2D(x, y));
            }

            lastX = x;
            lastY = y;
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            nw.lon = MathUtils.pixelToLon(x + map.nwPixels.lon, map.curZoom);
            nw.lat = MathUtils.pixelToLat(y + map.nwPixels.lat, map.curZoom);
            Rectangle r = new Rectangle(0, 0, 10, 10);
            Vector2D p;
            for(int i = 0; i < rect.corners.length; i++) {
                p = rect.corners[i];
                r.x = (int) p.x;
                r.y = (int) p.y;
                if(r.contains(e.getX(), e.getY())) {
                    colors[i] = Color.BLUE;
                } else {

                }
            }
            repaint();
        }
    }

    public class GRButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("OK")) {
                rect.figureExtent();
                BaseObject.sRegistry.controller.setCoords(rect.north, rect.south, rect.east, rect.west, (float) Math.toDegrees(rect.rotation));
                BaseObject.sRegistry.infoPanel.switchToGeoreference(false);
                setVisible(false);
                BaseObject.sRegistry.mainFrame.outputPanel.setVisible(true);
            } else if(command.equals("map_type")) {
                if(map.mapType.equals("sat")) {
                    mapTypeButton.setIcon(globe);
                    map.mapType = "osm";
                    map.dirty = true;
                } else {
                    mapTypeButton.setIcon(road);
                    map.mapType = "sat";
                    map.dirty = true;

                }
                map.figureTiles();
            } else if(command.equals("CANCEL")) {
            	goBack();
            }
        }
    }
}
