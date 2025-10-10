package cn.darven.spring.sub;

import cn.darven.spring.Autowired;
import cn.darven.spring.Component;
import cn.darven.spring.PostConstruct;

/**
 * @author darven
 * @date 2025/10/11
 * @description TODO
 */
@Component(name = "mydog")
public class Dog {

    // 循环依赖，出现StackOverflowError cat需要dog dog需要cat
    @Autowired
    private Cat cat;

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init(){
        System.out.println("dog init cat is "+cat);
    }
}
