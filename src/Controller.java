import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.SwingWorker;

class Controller implements ActionListener {
	private Model model;
	private View  view;

	private Timer                   timer;
	private SwingWorker<Void, Void> worker;

	Controller(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		JButton button = (JButton) evt.getSource();
		if (button.getText().equals("Start")) {
			startButtonClicked();
		}
		else {
			stopButtonClicked();
		}
	}

	private void startButtonClicked() {
		Body body1, body2;
		try {
			body1 = view.getBody1();
			body2 = view.getBody2();
		}
		catch (NumberFormatException ex) {
			ex.printStackTrace();
			return;
		}

		initializeSimulationPrerequisites();

		try {
			model.initializeBodies(body1, body2);
			model.simulateInitialHalfstep();
		}
		catch (DivisionByZeroException | IllegalArgumentException ex) {
			ex.printStackTrace();
			return;
		}
		view.button.setText("Stop");
		worker.execute();
	}

	private void initializeSimulationPrerequisites() {
		timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					model.simulateNextStep();
				}
				catch (DivisionByZeroException ex) {
					view.button.doClick();
					ex.printStackTrace();
				}
			}
		};
		worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				timer.schedule(task, 0, 1);
				return null;
			}
		};
	}

	private void stopButtonClicked() {
		timer.cancel();
		worker.cancel(false);
		view.button.setText("Start");
		try {
			Thread.sleep(10);
		}
		catch (Exception e) {
		}

		view.writer.writeAll(view.csvInput);
	}
}
