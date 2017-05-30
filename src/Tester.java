import java.awt.*;

public class Tester {
    public static void main(String[] args) {
        System.out.println(new Font("Tahoma", Font.PLAIN, 12).canDisplay('\25'));
        Console c = new Console();
        System.out.println(Console.Companion.getConsoles());
    }
}