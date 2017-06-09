
public class WarningMain2 {

	private static class TextClass {}

	public static void main(String[] args) {
		TextClass text2 = new TextClass();
		// useless casting for compiler warning generation
		TextClass text =  (TextClass) text2;
	}

}
