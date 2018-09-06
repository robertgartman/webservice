package se.avtalsbanken.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import se.avtalsbanken.Data;


@Controller
public class Message {

    @Autowired Data data;

    @GetMapping("/message")
    public String greeting() {
        return "greeting";
    }

}