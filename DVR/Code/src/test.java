import java.util.ArrayList;

public class test {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();

        list.add(3);
        list.add(4);
        list.add(5);

        for(int num: list) {
            if(num==4) continue;
            System.out.println(num);
        }
    }
}
