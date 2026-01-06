package com.test.client;
import com.tiny.rpc.annotation.RpcReference;
import com.rpc.api.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RpcReference // <--- 自动注入代理对象
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello() {
        return helloService.sayHello("Spring Consumer");
    }
}