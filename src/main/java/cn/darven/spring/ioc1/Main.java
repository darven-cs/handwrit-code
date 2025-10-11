package cn.darven.spring.ioc1;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author darven
 * @date 2025/10/10
 * @description TODO
 */
public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ApplicationContext applicationContext = new ApplicationContext("cn.darven.spring.ioc1");
        Object cat = applicationContext.getBean("cn.darven.spring.ioc1.sub.Cat");
        System.out.println("Cat: " + cat);
        Object dog = applicationContext.getBean("mydog");
        System.out.println("Dog: " + dog);
    }
}
