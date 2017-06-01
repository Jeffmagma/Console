public class Tester {
    public static void main(String[] args) {
        /*JFrame frame = new JFrame();
        frame.getContentPane().setPreferredSize(new Dimension(300, 300));
        frame.pack();
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(Color.RED);
                g.drawRect(0, 0, 2, 2);
            }
        };
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);*/
        Console c = new Console();
        System.out.println(c.readString());
    }
}