public class Tester {
    public static void main(String[] args) {
        Console c = new Console();
        c.drawString("lol", 2, 3);
        Console d = new Console();
        System.out.println(c.readString());
    }
}