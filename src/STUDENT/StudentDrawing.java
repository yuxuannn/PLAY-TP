package STUDENT;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class StudentDrawing extends JFrame implements MouseListener,
		MouseMotionListener, ActionListener {

	ArrayList<Point> strokeData = new ArrayList<Point>();
	ArrayList<Point> strokeDraw = new ArrayList<Point>();
	ArrayList<Point> pointStored = new ArrayList<Point>();
	ArrayList<Point> matchTemp = new ArrayList<Point>();
	static ArrayList<ArrayList<Point>> strokeArray = new ArrayList<ArrayList<Point>>();
	static ArrayList<ArrayList<Point>> indicatorArray = new ArrayList<ArrayList<Point>>();
	static int strokeArrayIndex = 0;
	static int indicatorArrayIndex = 0;
	int strokeCounter = 1;
	// int indicatorCounter = 0;
	int width;
	int height;
	ImagePanel imgPanel;
	int xRef, yRef;
	int xNew;
	int yNew;
	int x_offset = 10;
	int y_offset = 10;

	int tolerance = 20;
	boolean bolRecordPoint = false;
	boolean matchStoredPoint = false;
	int radius = 5;
	int Xcoord, Ycoord;
	static int strokeid = 0;
	int noOfStrokeMatched = 0;
	static String filename = "";
	private BufferedImage image;
	Document doc;
	boolean fill;
	boolean loadData = false;
	boolean matched = false;
	boolean displayIndicator = false;
	int buttonHeight;
	int noOfStrokes;
	String fname;
	BufferedImage img;
	File fi;
	int lengthTolerance = 40;

	public StudentDrawing() throws IOException {

		fileChooser(fi, img, fname);
		int index = fname.indexOf(".");
		filename = fname.substring(0, index);

		image = img;

		setTitle("Drawing of the Chinese Character Stroke");
		setSize(700, 700);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		width = image.getWidth();
		height = image.getHeight();

		xRef = (700 - image.getWidth()) / 2;
		yRef = (700 - image.getHeight()) / 2;

		JPanel conPanel = new JPanel();
		conPanel.setLayout(new GridLayout(1, 2));
		JButton button2 = new JButton("Match Strokes");
		button2.addActionListener(this);
		conPanel.add(button2);
		this.getContentPane().add(conPanel, BorderLayout.SOUTH);

		Dimension d = button2.getPreferredSize();
		buttonHeight = (int) d.getHeight();
		System.out.println("buttonHeight = " + buttonHeight);

		ImagePanel imgPanel = new ImagePanel(image, xRef, yRef);
		imgPanel.addMouseListener(this);
		imgPanel.addMouseMotionListener(this);
		imgPanel.setBounds(0, 0, 400, 400);
		this.getContentPane().add(imgPanel, BorderLayout.CENTER);

		try {

			File fXmlFile = new File("C:/resources/"+filename + ".xml");
			System.out.println("Reading of " + fXmlFile.getName());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// transfer indicator data from xml to 2-D indicator array
			System.out
					.println("transfer indicator data from xml to 2-D indicator array");

			NodeList indicatorList = doc.getElementsByTagName("indicator");
			noOfStrokes = indicatorList.getLength();
			for (int i = 0; i < indicatorList.getLength(); i++) {
				indicatorArray.add(new ArrayList<Point>());
				System.out.println("indicator " + (i + 1));
				Node indicatorNode = indicatorList.item(i);
				Element indicatorElement = (Element) indicatorNode;
				NodeList pointList = indicatorElement
						.getElementsByTagName("point");
				for (int j = 0; j < pointList.getLength(); j++) {
					System.out.println("point " + (j + 1));
					Node pointNode = pointList.item(j);
					Element pointElement = (Element) pointNode;

					Element xElement = (Element) pointElement
							.getElementsByTagName("x").item(0);
					int xInt = Integer.parseInt(((Node) xElement
							.getChildNodes().item(0)).getNodeValue());
					Element yElement = (Element) pointElement
							.getElementsByTagName("y").item(0);
					int yInt = Integer.parseInt(((Node) yElement
							.getChildNodes().item(0)).getNodeValue());

					// System.out.println("x : " + xInt);
					// System.out.println("y : " + yInt);

					Point dataPoint = new Point(xInt, yInt);
					indicatorArray.get(indicatorArrayIndex).add(dataPoint);
				}
				if (matched = true) {
					indicatorArrayIndex++;
				} else {

				}
				;
			}

			/*
			 * // for debugging
			 * System.out.println("Reading from 2D indicatorArray List");
			 * 
			 * for (int i = 0; i < indicatorArray.size(); i++) {
			 * System.out.println("Indicator: " + (i + 1)); for (int j = 0; j <
			 * indicatorArray.get(i).size(); j++) { System.out.println("x:" +
			 * indicatorArray.get(i).get(j).getX()); System.out.println("y:" +
			 * indicatorArray.get(i).get(j).getY()); } }
			 */

			// transfer stroke data from xml to 2-D stroke array
			System.out
					.println("transfer stroke data from xml to 2-D stroke array");
			NodeList strokeList = doc.getElementsByTagName("stroke");
			System.out.println("No of stroke =" + strokeList.getLength());
			for (int i = 0; i < strokeList.getLength(); i++) {
				strokeArray.add(new ArrayList<Point>());
				// System.out.println("stroke " + (i + 1));
				Node strokeNode = strokeList.item(i);
				System.out.println("Stroke " + i);
				Element strokeElement = (Element) strokeNode;
				NodeList pointStrokeList = strokeElement
						.getElementsByTagName("pointStroke");
				System.out.println("No of pointStroke for stroke " + i + " = "
						+ pointStrokeList.getLength());
				for (int j = 0; j < pointStrokeList.getLength(); j++) {
					// System.out.println("pointStroke " + (j + 1));
					Node pointStrokeNode = pointStrokeList.item(j);
					Element pointStrokeElement = (Element) pointStrokeNode;

					Element xElement = (Element) pointStrokeElement
							.getElementsByTagName("x").item(0);
					int xInt = Integer.parseInt(((Node) xElement
							.getChildNodes().item(0)).getNodeValue());
					Element yElement = (Element) pointStrokeElement
							.getElementsByTagName("y").item(0);
					int yInt = Integer.parseInt(((Node) yElement
							.getChildNodes().item(0)).getNodeValue());

					// System.out.print("x : " + xInt+"\t");
					// System.out.println("y : " + yInt);

					Point dataPoint = new Point(xInt, yInt);
					strokeArray.get(strokeArrayIndex).add(dataPoint);
				}
				strokeArrayIndex++;
			}
			/*
			 * // for debugging
			 * System.out.println("Reading from 2D strokeArray List");
			 * 
			 * for (int i = 0; i < strokeArray.size(); i++) {
			 * System.out.println("Stroke: " + (i + 1));
			 * System.out.println("no of  pointStroke = "
			 * +strokeArray.get(i).size()); for (int j = 0; j <
			 * strokeArray.get(i).size(); j++) {
			 * System.out.print("pointStroke "+(j+1)+"\tx:" +
			 * strokeArray.get(i).get(j).getX() +"\t"); System.out.println("y:"
			 * + strokeArray.get(i).get(j).getY()); } }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadData = true;
		System.out.println("loadData = " + loadData);
		repaint();
	}

	public void fileChooser(File f, BufferedImage image, String filename)
			throws IOException {

		System.out.println("Selection in progress");
		System.out.println("=====================");
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		f = chooser.getSelectedFile();
		filename = f.getName();

		System.out.println("You have selected: " + filename);
		image = ImageIO.read(f);

		fname = filename;
		img = image;

		return;

	}

	public void paint(Graphics g) {
		super.paintComponents(g);
		if (loadData) {
			// xRef = imgPanel.getxref();
			// yRef = imgPanel.getyref();
			//g.setStroke(new BasicStroke(10.0f));
		   // int lineWidth = getWidth();
	        //int lineHeight = getHeight();
			 //g.drawLine(0,0,lineWidth,lineHeight);    //default
		        Graphics2D g2 = (Graphics2D) g;
		        g2.setStroke(new BasicStroke(4));
		        //g2.drawLine(0,lineHeight,lineWidth,0);   //thick
			g.setColor(Color.RED);
			for (int i = 0; i < strokeDraw.size() - 1; i++) {
				g.drawLine((int) strokeDraw.get(i).getX(), (int) strokeDraw
						.get(i).getY() + 25,
						(int) strokeDraw.get(i + 1).getX(), (int) strokeDraw
								.get(i + 1).getY() + 25);
				// repaint();
			}

			// drawing of indicator

			if ((strokeCounter - 1) != noOfStrokes) {
				// System.out.println("strokeCounter = " + strokeCounter
				// + "\t no of indicator points = "
				// + indicatorArray.get(strokeCounter - 1).size());

				if (displayIndicator) {
					System.out.println("error detected = " + displayIndicator);
					for (int i = 0; i < indicatorArray.get(strokeCounter - 1)
							.size(); i++) {

						// System.out.println("indicator " + i);
						 Graphics2D g3 = (Graphics2D) g;
					        g3.setStroke(new BasicStroke(1));
						g.setColor(Color.blue);
						g.drawOval(((int) indicatorArray.get(strokeCounter - 1)
								.get(i).getX())
								+ xRef - radius,
								((int) indicatorArray.get(strokeCounter - 1)
										.get(i).getY())
										+ yRef + buttonHeight - radius,
								2 * radius, 2 * radius);

					}
				}
			}

			// drawing of teacher's stroke
			System.out.print("strokeCounter = " + strokeCounter + "\t");

			int offset = buttonHeight; // button Height

			if ((strokeCounter > 1) && ((strokeCounter - 1) <= noOfStrokes)) {
				for (int i = 0; i < (strokeCounter - 1); i++) {
					for (int j = 0; j < ((strokeArray.get(i).size()) - 1); j++) {
						// System.out.println("Teacher's stroke "+i);
						g.setColor(Color.GREEN);
						g.drawLine((int) strokeArray.get(i).get(j).getX()
								+ xRef, (int) strokeArray.get(i).get(j).getY()
								+ yRef + offset,
								(int) strokeArray.get(i).get(j + 1).getX()
										+ xRef,
								(int) strokeArray.get(i).get(j + 1).getY()
										+ yRef + offset);
					}
				}

			}

		}

	}

	public void mouseClicked(MouseEvent e) {

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
		// System.out.println("mouseDragged");

		int mx = e.getX();
		int my = e.getY();

		// System.out.println("xref : " + xRef);

		xNew = (mx - xRef);
		yNew = (my - yRef);

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
		if (actionCommand.equals("Match Strokes")) {
			System.out.println("Match Strokes");

			//System.out.println("Teacher : strokeArray.size()="+ strokeArray.get(strokeCounter - 1).size());
			System.out.println("Student : strokeData.size()="
					+ strokeData.size());
			System.out.println("strokeCounter = " + strokeCounter);

			if (strokeData.size() == strokeArray.get(strokeCounter - 1).size()) {
				for (int i = 0; i < strokeData.size(); i++) {

					System.out.println(strokeData.get(i).getX() + ", "
							+ strokeData.get(i).getY() + " | "
							+ strokeArray.get(strokeCounter - 1).get(i).getX()
							+ ", "
							+ strokeArray.get(strokeCounter - 1).get(i).getY());

					if ((Math.abs(strokeData.get(i).getX()
							- strokeArray.get(strokeCounter - 1).get(i).getX()) < tolerance)
							&& (Math.abs(strokeData.get(i).getY()
									- strokeArray.get(strokeCounter - 1).get(i)
											.getY()) < tolerance)) {

						matched = true;

					} else
						matched = false;

					// noOfStrokeMatched++;

				}
			} else {
				if (Math.abs((strokeData.size() - strokeArray.get(
						strokeCounter - 1).size())) < lengthTolerance) {

					for (int i = 0; i < (Math.min(strokeData.size(),
							strokeArray.get(strokeCounter - 1).size())); i++) {

						System.out.print("strokeData[" + i + "]\t");

						System.out.println(strokeData.get(i).getX()
								+ ", "
								+ strokeData.get(i).getY()
								+ " | "
								+ strokeArray.get(strokeCounter - 1).get(i)
										.getX()
								+ ", "
								+ strokeArray.get(strokeCounter - 1).get(i)
										.getY());

						if ((Math.abs(strokeData.get(i).getX()
								- strokeArray.get(strokeCounter - 1).get(i)
										.getX()) < tolerance)
								&& (Math.abs(strokeData.get(i).getY()
										- strokeArray.get(strokeCounter - 1)
												.get(i).getY()) < tolerance)) {
							matched = true;

						} else {
							matched = false;

						}
						System.out.println("xDiff="
								+ (Math.abs(strokeData.get(i).getX()
										- strokeArray.get(strokeCounter - 1)
												.get(i).getX()))
								+ "\tyDiff="
								+ (Math.abs(strokeData.get(i).getY()
										- strokeArray.get(strokeCounter - 1)
												.get(i).getY())) + "\tMatch="
								+ matched);

					}

					if (matched) {

						displayIndicator = false;

						System.out.println("Matched = " + matched);
						System.out.println("Stroke " + strokeCounter
								+ " completed!");

						strokeCounter++;
						System.out.println("Increasing strokeCounter to "
								+ strokeCounter);

					} else {
						JOptionPane.showMessageDialog(this,
								"Stroke error. Please try again!",
								"Error", JOptionPane.PLAIN_MESSAGE);
						displayIndicator = true;
					}
				}
				else {
					// stroke incomplete
					JOptionPane.showMessageDialog(this,
							"Stroke error. Please try again!",
							"Error", JOptionPane.PLAIN_MESSAGE);
					displayIndicator = true;
				}
				

				strokeData.clear();
				strokeDraw.clear();
				repaint();
			}

			if (noOfStrokes == strokeCounter - 1) {
				loadData = true;

				JOptionPane.showMessageDialog(this, "Done!", "Done",
						JOptionPane.PLAIN_MESSAGE);

			}
		} else {

		}

	}

	public static void main(String[] args) throws IOException {

		StudentDrawing runthis = new StudentDrawing();

	}

}
