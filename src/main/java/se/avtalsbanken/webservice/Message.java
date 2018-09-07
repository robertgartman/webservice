package se.avtalsbanken.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import se.avtalsbanken.Data;

@RestController
public class Message {

    @Autowired Data data;

    @GetMapping("/message")
    public String greeting() {
        return "greeting";
    }

}