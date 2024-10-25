package com.example.formapp.controller;

import com.example.formapp.model.Form;
import com.example.formapp.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class FormController {

    @Autowired
    private FormService formService;

    @GetMapping("/")
    public String index(Model model) throws IOException {
        model.addAttribute("forms", formService.getFormList());
        return "index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new Form());
        return "create-form";
    }

    @PostMapping("/save")
    public String saveForm(@ModelAttribute Form form) throws IOException {
        formService.saveForm(form);
        return "redirect:/";
    }

    @GetMapping("/edit/{name}")
    public String editForm(@PathVariable String name, Model model) throws IOException {
        Form form = formService.getForm(name);
        if (form == null) {
            return "redirect:/";
        }
        model.addAttribute("form", form);
        return "create-form";
    }
}
