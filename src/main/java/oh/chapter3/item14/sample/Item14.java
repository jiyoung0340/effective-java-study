package oh.chapter3.item14.sample;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.TreeSet;



public class Item14 {
    public static void main(String[] args){
        BigDecimal bd1 = new BigDecimal("1.0");
        BigDecimal bd2 = new BigDecimal("1.00");

        HashSet hs = new HashSet<>();
        TreeSet ts = new TreeSet();

        hs.add(bd1);
        hs.add(bd2);

        ts.add(bd1);
        ts.add(bd2);

        System.out.println(hs.size());
        System.out.println(ts.size());
    }
}
