import java.util.logging.Logger;

public class WarningMain {
	
	private final static Logger LOG = Logger.getLogger(WarningMain.class.getName());

	private static class TextClass {
		private String test = "Hallo Welt!";

		@Deprecated
		public String getText() {
			return test;
		}
		
		
	}
	
	public static void main(String[] args) {
		
		// unused variable for compiler warning generation
		int i;
		
		TextClass text = new TextClass();
		
		// useless casting for compiler warning generation
		TextClass text2 = (TextClass) text;
		
		// using deprecated method  for compiler warning generation
		LOG.warning(text2.getText());

	}

}
