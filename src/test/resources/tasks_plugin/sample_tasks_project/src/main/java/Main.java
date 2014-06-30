
public class Main {

    public static final int EIGHT = 8;

    public static void main(String[] args) {
        System.out.println("Running....");

        //TODO: remove useless if or replace with meaningful condition
	if(true) {
            System.out.println("True!");
        }

	System.out.println("...Finished.");

    }

    //FIXME: returns 6 instead of 7
    public static int return7() {
        return 6;
    }

    @Deprecated
    public static int return8() {
        return EIGHT;
    }
}
