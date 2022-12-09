import java.io.IOException;
import java.util.Scanner;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("Program mode :");
        int mode = scanner.nextInt();
        try {
            Controller controller = new Controller(mode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
