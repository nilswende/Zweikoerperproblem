import com.opencsv.CSVWriter;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

class View extends JFrame implements Observer {
	JButton    button;
	JTextField massField1, posXField1, posYField1, velXField1, velYField1;
	JTextField massField2, posXField2, posYField2, velXField2, velYField2;

	List<String[]> csvInput;
	CSVWriter writer;

	View() {
		//TODO generate the GUI
		super("Keplerproblem");
		button = new JButton();
		button.setText("Start");
		add(button);

		csvInput = new LinkedList<>();
		try {
			writer = new CSVWriter(new FileWriter("output.csv"));
		}
		catch (IOException ex) {

		}
		String[] line = {"ErdeX", "Erde", "MondX", "Mond", "BaryX", "Baryzentrum", "Flaeche1", "Flaeche2"};
		csvInput.add(line);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(110,70);
		setLocation(500,500);
		setVisible(true);
	}

	@Override
	public void update(Observable obs, Object arg) {
		Model model = (Model) obs;
		Body[] bodies = model.getBodies();
		double[] areas = model.getAreas();
		Body body1 = bodies[0];
		Body body2 = bodies[1];
		Body bary = bodies[2];
		double area1 = areas[0];
		double area2 = areas[1];
		String[] line = {String.format("%.12f", body1.xPos/1000), String.format("%.12f", body1.yPos/1000),
				String.format("%.12f", body2.xPos/1000), String.format("%.12f", body2.yPos/1000),
				String.format("%.12f", bary.xPos/1000), String.format("%.12f", bary.yPos/1000),
				String.format("%.12f", area1), String.format("%.12f", area2)};
		csvInput.add(line);
	}

/*
	//Sonne
	Body getBody1() {
		Body body = new Body();
		try {
			body.mass = 1988500e24;
			body.xPos = 152.1e9; // apoapsis
			body.yPos = 0;
			body.xVel = 0;
			body.yVel = -29.29e3/333000;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}

	//Erde
	Body getBody2() {
		Body body = new Body();
		try {
			body.mass = 5.974e24;
			body.xPos = 0;
			body.yPos = 0;
			body.xVel = 0;
			body.yVel = 29.29e3;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}
*/

	//Erde
	Body getBody1() {
		Body body = new Body();
		try {
			body.mass = 5.9726e24;
			body.xPos = 0.4055e9; // apoapsis in meter
			body.yPos = 0.4333e9;
			body.xVel = 0;
			body.yVel = -11.86188;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}

	//Mond
	Body getBody2() {
		Body body = new Body();
		try {
			body.mass = 7.3492e22; //kg
			body.xPos = 0;
			body.yPos = 0.4333e9;
			body.xVel = 0;
			body.yVel = 964; // m/s
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}

/*
	Body getBody1() {
		Body body = new Body();
		try {
			body.mass = Double.parseDouble(massField1.getText());
			body.xPos = Double.parseDouble(posXField1.getText());
			body.yPos = Double.parseDouble(posYField1.getText());
			body.xVel = Double.parseDouble(velXField1.getText());
			body.yVel = Double.parseDouble(velYField1.getText());
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}

	Body getBody2() {
		Body body = new Body();
		try {
			body.mass = Double.parseDouble(massField2.getText());
			body.xPos = Double.parseDouble(posXField2.getText());
			body.yPos = Double.parseDouble(posYField2.getText());
			body.xVel = Double.parseDouble(velXField2.getText());
			body.yVel = Double.parseDouble(velYField2.getText());
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
		return body;
	}
*/

	void addController(ActionListener controller) {
		button.addActionListener(controller);
	}
}
