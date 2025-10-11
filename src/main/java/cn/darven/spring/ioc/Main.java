package cn.darven.spring.ioc;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author darven
 * @date 2025/10/10
 * @description TODO
 */
public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ApplicationContext applicationContext = new ApplicationContext("cn.darven.spring");
//        Object cat = applicationContext.getBean("Cat");
//        System.out.println(cat);
//        Object dog = applicationContext.getBean("mydog");
//        System.out.println(dog);
    }
}
