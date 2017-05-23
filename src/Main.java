public class Main {
	public static void main(String[] args) {
		Model model = new Model();
		View view = new View();

		model.addObserver(view);

		Controller controller = new Controller(model, view);

		view.addController(controller);
	}
}
