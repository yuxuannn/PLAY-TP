package CANNYEDGE;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import TEACHER.TeacherDrawing;


public class ui {
	
	static String filename ;

	public ui(String name) throws IOException {
		// TODO Auto-generated method stub
/*		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File f = chooser.getSelectedFile();
		String filename = f.getName();
		System.out.println("You have selected: " +filename);
*/		
		
		filename = name;
		System.out.println("Get Image");
		System.out.println(name);
	//	BufferedImage image =  getImageFromFile();
		File f = new File("C:/resources/"+name+".jpg");
		BufferedImage image = ImageIO.read(f);
		System.out.println("You have selected: " +name);
		

		
		
		//BufferedImage image = ImageIO.read(f);
		
		System.out.println("Get Edge");
		BufferedImage imageEdge = EdgeDetector(image);
		
					
				//save image to folder
		System.out.println("Save Image");
		System.out.println(filename+".jpg");

		ImageIO.write(imageEdge, "jpeg", new File ("C:/resources/"+filename+".jpg"));
		
		xmledge();
		
	}
	
/*	public static BufferedImage getImageFromFile() throws IOException{
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File f = chooser.getSelectedFile();
		filename = f.getName();
		System.out.println("You have selected: " +filename);
		BufferedImage image = ImageIO.read(f);
		return(image);

	}
	*/
	
	public static BufferedImage EdgeDetector(BufferedImage image) throws IOException {
		
	//create the detector
	cannyEdgeDetector detector = new cannyEdgeDetector();

	//adjust its parameters as desired
	detector.setLowThreshold(0.5f);
	detector.setHighThreshold(1f);

	//apply it to an image
	detector.setSourceImage(image);
	detector.process();
	BufferedImage edges = detector.getEdgesImage();
	return(edges);
	
	
	
	}

		public static void xmledge() throws IOException {
			 
			  try {
		 
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		 
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("Lesson");
				doc.appendChild(rootElement);
		 
				// staff elements
				Element element = doc.createElement("Image");
				element.appendChild(doc.createTextNode(filename));
				rootElement.appendChild(element);
		 
			
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult("C:/resources/"+filename+".xml");
		 
				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);
		 
				transformer.transform(source, result);
		 
				TeacherDrawing runthis = new TeacherDrawing(filename);
		 
			  } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			  } catch (TransformerException tfe) {
				tfe.printStackTrace();
			  }
			  
			  System.out.println("File saved!");
			

	}

}
	
