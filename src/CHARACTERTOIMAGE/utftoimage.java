package CHARACTERTOIMAGE;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.JOptionPane;

import CANNYEDGE.ui;
import CHARACTERTOIMAGE.utftoimage;

public class utftoimage {

String transferredChar, file, filename, lessonname;

public utftoimage(String data,String name,String lesson) throws IOException {
	
	System.out.println("Text received : "+data);
    
	lessonname = lesson;

    try {
    	int fontSize = 200;
    	Font font  = new  Font("kaiti", Font.PLAIN, fontSize);
    	BufferedImage bufferedImage = new BufferedImage(179, 231,
    	BufferedImage.TYPE_INT_RGB);
    	Graphics2D g = bufferedImage.createGraphics();
    	g.setColor( Color.WHITE );
    	g.fillRect(0,0,179,231);
    	g.setColor(Color.black);
    	g.setFont(font);
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g.drawString(data,(((bufferedImage.getWidth())/2)-100),((bufferedImage.getHeight())/2)+70);
    	g.dispose();
    	ImageIO.write(bufferedImage, "JPG", new File("C:/resources/"+name+".jpg"));
    	System.out.println("Image created , name : "+name+" , character : "+data+" , path : C:/resources"+name+".jpg");
    	System.out.println("Passing to Image Manipulation");

    	//file is with .jpg
    	file = lessonname+"/Parts/"+name+".jpg";
    	//filename is without
    	filename = name;
    	
    	ui runthis = new ui(filename);

    	} catch (Exception e) {
    	e.printStackTrace();
    	}
    }


}




//http://bytes.com/topic/java/answers/799430-drawstring-chinese-characters