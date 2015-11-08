import java.util.*;

public class Test {



    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(4,"hey");
        for (int i : map.keySet()) {
            System.out.println(i);
        }
    }
}
