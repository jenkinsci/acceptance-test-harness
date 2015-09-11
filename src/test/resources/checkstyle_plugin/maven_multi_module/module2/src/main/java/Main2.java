
public class Main2 {

    public static final int EIGHT = 8;

    public static void main(String[] args) {
        System.out.println("Running....");

        if(true) {
            System.out.println("True!");
        }

        if(false) {
            throw new RuntimeException();
        }

        Object o = new Object();
        if(o == null) {
            System.out.printf("O not null");
        }
    }

    public static int return7() {
        return 7;
    }

    public static int return8() {
        return EIGHT;
    }
}
