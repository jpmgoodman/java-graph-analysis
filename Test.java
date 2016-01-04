import java.util.*;

public class Test {



    public static void main(String[] args) {
        LinkedList<Integer> ls = new LinkedList<Integer>();
        ls.add(5);
        ls.add(6);
        ls.poll();
        System.out.println(ls.get(0));
    }
}
