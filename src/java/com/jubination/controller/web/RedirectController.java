package com.jubination.controller.web;



import com.jubination.init.Init;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Controller;



@Controller
public class RedirectController {

    public RedirectController() {
    }
    
    @PostConstruct
    public void init() {
        System.out.println("*******com.jubination.controller.web.RedirectController.init()");
        Init.main(null);
    }
    
}
