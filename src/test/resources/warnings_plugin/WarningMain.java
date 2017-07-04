
public class WarningMain {

	private static class TextClass {}

	public static void main(String[] args) {
		TextClass text = new TextClass();
		TextClass text2 = new TextClass();
		// useless casting for compiler warning generation
		text =  (TextClass) text2;
		text2 = (TextClass) text;

		Integer inte = (int) 123L;
		inte = inte/0;
	}

}
