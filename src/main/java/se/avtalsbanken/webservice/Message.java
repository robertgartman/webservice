package se.avtalsbanken;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.avtalsbanken.Demo;

@Controller
public class MessageController {

    @autowire Data data;

    @GetMapping("/message")
    public String greeting() {
        return "greeting";
    }

}