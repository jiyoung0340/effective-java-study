package oh.chapter3.item10.sample;

import java.net.URL;

public class mainItem10 {
    public static void main(String[] args) {
        Point p = new Point(1, 2);
        ColorPoint cp = new ColorPoint(1, 2, Color.RED);

        boolean b1 = p.equals(cp);
        boolean b2 = cp.equals(p);

        System.out.println(b1);
        System.out.println(b2);

    }
}
