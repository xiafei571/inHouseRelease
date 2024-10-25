package com.example.formapp.controller;

import com.example.formapp.model.Form;
import com.example.formapp.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FormController {

    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

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
    public String saveForm(@RequestParam Map<String, String> allParams) throws IOException {
        logger.debug("Received all parameters: {}", allParams);

        Form form = new Form();
        form.setName(allParams.get("name"));
        logger.debug("Form name: {}", form.getName());

        List<Form.Variable> variables = new ArrayList<>();
        List<Form.Command> commands = new ArrayList<>();

        // Process variables
        int i = 0;
        while (null != allParams && allParams.containsKey("variableNotes[" + i + "]")) {
            String notes = allParams.get("variableNotes[" + i + "]");
            String key = allParams.get("variableKeys[" + i + "]");
            String value = allParams.get("variableValues[" + i + "]");
            
            logger.debug("Checking variable {}: notes={}, key={}, value={}", i, notes, key, value);
            
            if (notes == null || key == null || value == null) {
                logger.debug("No more variables found after index {}", i);
                break;
            }

            if (notes == null) notes = allParams.get("variableNotess[" + i + "]");
            if (key == null) key = allParams.get("variablekeys[" + i + "]");
            if (value == null) value = allParams.get("variablevalues[" + i + "]");
            
            Form.Variable variable = new Form.Variable();
            variable.setNotes(notes);
            variable.setKey(key);
            variable.setValue(value);
            variables.add(variable);
            logger.debug("Added variable {}: notes={}, key={}, value={}", 
                i, variable.getNotes(), variable.getKey(), variable.getValue());
            i++;
        }

        // Process commands
        int j = 0;
        while (null != allParams && allParams.containsKey("commandNotes[" + j + "]")) {
            String notes = allParams.get("commandNotes[" + j + "]");
            String content = allParams.get("commandContents[" + j + "]");
            
            logger.debug("Checking command {}: notes={}, content={}", j, notes, content);
            
            if (notes == null || content == null) {
                logger.debug("No more commands found after index {}", j);
                break;
            }
            
            Form.Command command = new Form.Command();
            command.setNotes(notes);
            command.setContent(content);
            command.setActive(allParams.containsKey("commandActive[" + j + "]"));
            commands.add(command);
            logger.debug("Added command {}: notes={}, content={}, active={}", 
                j, command.getNotes(), command.getContent(), command.isActive());
            j++;
        }

        form.setVariables(variables);
        form.setCommands(commands);

        logger.debug("Processed form: {}", form);
        logger.debug("Variables count: {}", variables.size());
        logger.debug("Commands count: {}", commands.size());

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
