import java.util.HashMap;

/**
 * @author: lifs
 * @create: 2018-08-05 23:56
 **/
public class Demo {

    public static final String STATIC_DATA = "hello world";
    private String str1;
    private String str2;
    private int num1;
    private int num2;

    public static void main(String[] args) {
        HashMap hashMap = new HashMap();
    }

    private void sayHello1() {
        System.out.println("this is method1...");
    }

    private void sayHello2() {
        System.out.println("this is method2...");
    }

    public void sayHello3() {
        System.out.println("this is method3...");
    }
}
