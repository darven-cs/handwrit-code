package cn.darven.spring.sub;

import cn.darven.spring.Autowired;
import cn.darven.spring.Component;
import cn.darven.spring.PostConstruct;

/**
 * @author darven
 * @date 2025/10/10
 * @description TODO
 */
@Component
public class Cat {

    // 实现自动注入
    @Autowired
    private Dog dog;

    // 生命周期
    @PostConstruct
    public void init(){
        System.out.println("cat init dog is "+dog);
    }
}
