package TEACHER;

import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class TeacherDrawing extends JFrame implements MouseListener,
		MouseMotionListener, ActionListener {

	ArrayList<Point> strokeData = new ArrayList<Point>();
	ArrayList<Point> strokeStored = new ArrayList<Point>();
	ArrayList<Point> strokeDraw = new ArrayList<Point>();
	ArrayList<Point> pointStored = new ArrayList<Point>();
	ArrayList<Point> pointDraw = new ArrayList<Point>();
	ArrayList<Point> matchTemp = new ArrayList<Point>();
	ArrayList<ArrayList<Point>> matchStored = new ArrayList<ArrayList<Point>>();

	int xref;
	int yref;
	int width;
	int height;

	int xNew;
	int yNew;
	int x_offset = 10;
	int y_offset = 10;

	int tolerance = 15;
	boolean bolRecordPoint = false;
	boolean matchStoredPoint = false;
	int radius = 5;
	int Xcoord, Ycoord;
	static int strokeid = 0;

	// int i = 0;
	int noOfStrokeMatched = 0;
	Document doc;
	int indicatorCounter = 0;
	int strokeCounter = 0;
	Element indicators;
	Element strokes;
	Element stroke;
	Element img;
	static String filename = "";

	private BufferedImage image;

	ImagePanel imgPanel;
	int imgPanelwidth = 700;
	int imgPanelHeight = 700;
	int xRef, yRef; 	// Image Reference Point
	int buttonHeight;

	public TeacherDrawing(String fileN) throws IOException {
		System.out.println("TeacherDrawing filename received = "+filename);
        filename = fileN;
		// Creation of DOM document
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

			// root elements
			Element root = doc.createElement("word");
			doc.appendChild(root);
			root.setAttribute("name",filename);

			// staff elements

			img = doc.createElement("image");
			img.setAttribute("src", filename);
			root.appendChild(img);

			strokes = doc.createElement("strokes");
			// System.out.println("Strokes");
			root.appendChild(strokes);

			indicators = doc.createElement("indicators");
			// System.out.println("Indicators");
			root.appendChild(indicators);

		} catch (Exception e) {
			System.out.println("Creation of DOM document Exception : " + e);
		}
		
		setTitle("Recording of the Chinese Character Stroke");
		setSize(700, 700);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		// create control panel
		JPanel conPanel = new JPanel();
		conPanel.setLayout(new GridLayout(1, 2));
		JButton button1 = new JButton("Record Strokes");
		JButton button3 = new JButton("Record Points");
		JButton button4 = new JButton("Save Points");
		JButton button5 = new JButton("Save All Points");
		//JButton button6 = new JButton("Clear Data");
		
		Dimension d = button1.getPreferredSize();
		buttonHeight = (int)d.getHeight();
		System.out.println("buttonHeight = "+buttonHeight);

		button1.addActionListener(this);
		button3.addActionListener(this);
		button4.addActionListener(this);
		button5.addActionListener(this);
		//button6.addActionListener(this);

		conPanel.add(button1);
		conPanel.add(button3);
		conPanel.add(button4);
		conPanel.add(button5);
		//conPanel.add(button6);

		this.getContentPane().add(conPanel, BorderLayout.SOUTH);
		
			System.out.println(filename);
		//	BufferedImage image =  getImageFromFile();
			File f = new File("C:/resources/"+filename+".jpg");
			BufferedImage image = ImageIO.read(f);
			System.out.println("You have selected: " +filename);
		
		

		xRef = (imgPanelwidth - image.getWidth()) / 2;
		yRef = ((imgPanelHeight) - image.getHeight()) / 2;
		// create image panel
		imgPanel = new ImagePanel(image, xRef, yRef);
		imgPanel.addMouseListener(this);
		imgPanel.addMouseMotionListener(this);
		imgPanel.setBounds(0, 0, imgPanelwidth, imgPanelHeight);
		this.getContentPane().add(imgPanel, BorderLayout.CENTER);

		repaint();
	}
/*
	public static BufferedImage getImageFromFile() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File f = chooser.getSelectedFile();
		filename = f.getName();
		System.out.println("You have selected: " + filename);
		BufferedImage image = ImageIO.read(f);
		return (image);

	}
	
	*/

	public void paint(Graphics g) {
		super.paintComponents(g);

		// System.out.println("xref : "+xRef);
		// System.out.println("yref : "+yRef);

		// draw black image border
		//g.setColor(Color.black);
		//g.drawRect(xRef, yRef, image.getWidth(), image.getHeight());
 
		Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));
		g.setColor(Color.red);
		for (int i = 0; i < strokeDraw.size() - 1; i++) {
			g.drawLine((int) strokeDraw.get(i).getX(), (int) strokeDraw.get(i)
					.getY() + 25, (int) strokeDraw.get(i + 1).getX(),
					(int) strokeDraw.get(i + 1).getY() + 25);
		}
		Graphics2D g3 = (Graphics2D) g;
	       g3.setStroke(new BasicStroke(1));
		g.setColor(Color.blue);
		if (bolRecordPoint == true) {
			for (int i = 0; i < pointDraw.size(); i++) {
				g.drawOval((int) pointDraw.get(i).getX() - radius,
						(int) pointDraw.get(i).getY() - radius + 25,
						2 * radius, 2 * radius);
			}

		}
	}                  

	public void drawCircle(int x, int y, Object source, boolean fill) {
		if (source instanceof JPanel) {
			Graphics g = ((JPanel) source).getGraphics();

			g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
			g.setColor(Color.BLACK);
			if (fill) {
				g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
			}
		}
		;// else ignore
	}

	public void mouseClicked(MouseEvent e) {
		if (bolRecordPoint == true) {
			Xcoord = e.getX();
			Ycoord = e.getY();

			xNew = Xcoord;
			yNew = Ycoord;

			System.out.println(Xcoord);
			System.out.println(Ycoord);

			Point dataDraw = new Point(Xcoord, Ycoord);
			pointDraw.add(dataDraw);

			Point dataPoint = new Point(xNew, yNew+25);
			pointStored.add(dataPoint);
		}

		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		System.out.println("mouseReleased");

		System.out.println("strokeData");
		System.out.println(strokeData.size());
		for (int i = 0; i < strokeData.size(); i++) {
			Point point = strokeData.get(i);
			System.out.println(point);
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		int mx = e.getX();
		int my = e.getY();

		// System.out.println("New x "+xRef);
		// System.out.println("new  y" +yRef);

		xNew = (mx - xRef);
		yNew = (my - yRef);

		/*
		 * if ((Math.abs(mx - x_old) > x_offset) || (Math.abs(my - y_old) >
		 * y_offset)) {
		 */
		if ((Math.abs(xNew) > x_offset) || (Math.abs(yNew) > y_offset)) {

			System.out.print("mx = " + mx + ", ");
			System.out.print("my = " + my + " | ");
			System.out.print("xNew = " + (mx - xRef) + ", ");
			System.out.println("yNew = " + (my - yRef) + " | ");

			Point dataDraw = new Point(mx, my);
			strokeDraw.add(dataDraw);

			Point dataPoint = new Point(xNew, yNew);
			strokeData.add(dataPoint);

		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		if (actionCommand.equals("Record Strokes")) {
			System.out.println("Record pressed");
			strokeStored.clear();
			// strokeStored = strokeData;
			for (int i = 0; i < strokeData.size(); i++) {
				strokeStored.add(strokeData.get(i));
			}
			strokeData.clear();
			System.out.println("strokeStored");

			strokeCounter++;

			Element stroke = doc.createElement("stroke");
			Attr strokeAttr = doc.createAttribute("id");
			strokeAttr.setValue(Integer.toString(strokeCounter));
			stroke.setAttributeNode(strokeAttr);
			strokes.appendChild(stroke);

			for (int i = 0; i < strokeStored.size(); i++) {
				Point point =                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     strokeStored.get(i);
				System.out.println(point);

				Element pointStroke = doc.createElement("pointStroke");
				Attr pointAttr = doc.createAttribute("id");
				pointAttr.setValue(Integer.toString(i + 1));
				pointStroke.setAttributeNode(pointAttr);
				stroke.appendChild(pointStroke);

				Element xCord = doc.createElement("x");
				xCord.appendChild(doc.createTextNode(Integer.toString((int)(strokeStored.get(i).getX()))));
				pointStroke.appendChild(xCord);

				Element yCord = doc.createElement("y");
				yCord.appendChild(doc.createTextNode(Integer.toString((int)(strokeStored.get(i).getY()))));
				pointStroke.appendChild(yCord);
			}
		
					
			strokeStored.clear();
			strokeDraw.clear();
			bolRecordPoint = false;
			repaint();

		} else if (actionCommand.equals("Record Points")) {
			bolRecordPoint = true;

			for (int i = 0; i < (Math.min(pointStored.size(),
					pointStored.size())) - 1; i++) {
				System.out.println(pointStored.get(i).getX() + ", "
						+ pointStored.get(i).getY() + " | "
						+ pointStored.get(i).getX() + ", "
						+ pointStored.get(i).getY());
				if ((Math.abs(pointStored.get(i).getX()
						- pointStored.get(i).getX()) < tolerance)
						&& (Math.abs(pointStored.get(i).getX()
								- pointStored.get(i).getX()) < tolerance)) {
					bolRecordPoint = true;
				} else
					bolRecordPoint = false;
			}
		}

		else if (actionCommand.equals("Save Points")) {
			System.out.println("Save Points");
			indicatorCounter++;
			// Coding needed to clear the indicators

			Element indicator = doc.createElement("indicator");
			Attr indicatorAttr = doc.createAttribute("id");
			indicatorAttr.setValue(Integer.toString(indicatorCounter));
			indicator.setAttributeNode(indicatorAttr);

			indicators.appendChild(indicator);

			// System.out.println("Indicator");

			for (int i = 0; i < pointStored.size(); i++) {

				Element point = doc.createElement("point");
				Attr pAttr = doc.createAttribute("id");
				pAttr.setValue(Integer.toString(i + 1));
				point.setAttributeNode(pAttr);

				indicator.appendChild(point);

				Element xCord = doc.createElement("x");
				// System.out.println("xCord="+Double.toString(pointStored.get(i).getX()));
				xCord.appendChild(doc.createTextNode(Integer.toString((int)(pointStored.get(i).getX())-xRef)));
				point.appendChild(xCord);
				
				Element yCord = doc.createElement("y");
				// System.out.println("yCord="+Double.toString(pointStored.get(i).getY()));
				yCord.appendChild(doc.createTextNode(Integer.toString((int)(pointStored.get(i).getY())-yRef-buttonHeight)));
				point.appendChild(yCord);
			}
			pointStored.clear();
			pointDraw.clear();
			bolRecordPoint = false;
			repaint();

		}
		
		else if (actionCommand.equals("Clear Data")) {
			System.out.println("Clear Data");
			
			strokeData.clear();
			//strokeData = null;
			System.out.println(strokeData);
			strokeStored.clear();
			//strokeStored = null;
			strokeDraw.clear();
			//strokeDraw = null;
			pointStored.clear();
			//pointStored = null;
			pointDraw.clear();
			//pointDraw = null;
			matchTemp.clear();
			//matchTemp = null;
			matchStored.clear();
			//matchStored = null;
			noOfStrokeMatched = 0;
			
			indicatorCounter = 0;
			strokeCounter = 0;
             
			

	}

		else if (actionCommand.equals("Save All Points")) {
			System.out.println("Save All Points");

			try {
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer aTransformer = transformerFactory.newTransformer();
				aTransformer.setOutputProperty("indent", "yes");

				DOMSource src = new DOMSource(doc);
				System.out.println("Printing to file");
				// StreamResult dest = new
				// StreamResult("C:\\Users\\oct12mpsip\\workspace\\2B12043A\\file.xml");
				StreamResult dest = new StreamResult(("C:/resources/"+filename+".xml"));
				aTransformer.transform(src, dest);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
			} catch (Exception e) {
				e.printStackTrace();

				System.out.println("XML file created!");
				
				}	
			}
			
		}

	}

	

