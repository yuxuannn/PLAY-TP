package TEACHER;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


public class ImagePanel extends JPanel{
	BufferedImage image;
	 int x;
	 int y;
	
    public ImagePanel(BufferedImage image, int xRef, int yRef) {
    	//setPreferredSize(new Dimension(400, 400));
        this.image = image;
        this.x = xRef;
        this.y = yRef;
    }
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.white);
        // Draw image centered.
        
        
        //x = (700 - image.getWidth())/2;
		//y = (700 - image.getHeight()) / 2;
        
        //System.out.println("image Width = "+image.getWidth());
        //System.out.println("image Height = "+image.getHeight());
        //System.out.println("x = "+x);
        //System.out.println("y = "+y);
        //int xRef = x;
        //int yRef = y;
        
        
        
        //System.out.println("getWidth="+getWidth()+" ,getHeight="+getHeight());
        //System.out.println("imageWidth="+image.getWidth()+" ,imageHeight="+image.getHeight());
        //System.out.println("x = " + x + ", y = "+ y);
        //x=0;
        //y=0;
        g.drawImage(image, x, y, this);
    }


    public int getxref(){
    	return x;
}

    public int getyref(){
    	return y;
    }
}

