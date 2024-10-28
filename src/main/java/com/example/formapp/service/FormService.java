package com.example.formapp.service;

import com.example.formapp.model.Form;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FormService {
    private static final Logger logger = LoggerFactory.getLogger(FormService.class);
    private static final String SOURCE_DIR = "source";
    private static final String OUTPUT_DIR = "output";

    public void saveForm(Form form) throws IOException {
        logger.debug("Saving form: {}", form);
        File sourceDir = new File(SOURCE_DIR);
        File outputDir = new File(OUTPUT_DIR);
        if (!sourceDir.exists()) {
            sourceDir.mkdir();
        }
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        String ymlFileName = form.getName() + ".yml";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", form.getName());
        
        List<Map<String, String>> variablesList = new ArrayList<>();
        for (Form.Variable variable : form.getVariables()) {
            Map<String, String> variableMap = new LinkedHashMap<>();
            variableMap.put("notes", variable.getNotes());
            variableMap.put("key", variable.getKey());
            variableMap.put("value", variable.getValue());
            variablesList.add(variableMap);
        }
        data.put("variables", variablesList);

        List<Map<String, Object>> commandsList = new ArrayList<>();
        for (int i = 0; i < form.getCommands().size(); i++) {
            Form.Command command = form.getCommands().get(i);
            Map<String, Object> commandMap = new LinkedHashMap<>();
            commandMap.put("module", "commandModule_" + (i + 1));
            commandMap.put("notes", command.getNotes());
            commandMap.put("content", command.getContent());
            commandMap.put("active", command.isActive());  // 确保这里正确保存了 active 状态
            commandsList.add(commandMap);
        }
        data.put("commands", commandsList);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(new File(sourceDir, ymlFileName))) {
            yaml.dump(data, writer);
        }
        logger.debug("Form saved successfully as YAML");

        // Generate .bat file in output directory
        String batFileName = form.getName() + ".bat";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, batFileName)))) {
            writer.write("@echo off");
            writer.newLine();
            writer.newLine();

            // Write variables
            for (Form.Variable variable : form.getVariables()) {
                writer.write("rem " + variable.getNotes());
                writer.newLine();
                writer.write("set " + variable.getKey() + "=" + variable.getValue());
                writer.newLine();
                writer.newLine();
            }

            // Write commands
            for (int i = 0; i < form.getCommands().size(); i++) {
                Form.Command command = form.getCommands().get(i);
                String moduleLabel = "commandModule_" + (i + 1);
                
                if (!command.isActive()) {
                    writer.write("goto " + moduleLabel);
                } else {
                    writer.write("rem goto " + moduleLabel);
                }
                writer.newLine();
                
                writer.write("rem " + moduleLabel);
                writer.newLine();
                writer.write("echo " + command.getNotes());
                writer.newLine();
                writer.write(command.getContent());
                writer.newLine();
                writer.write(":" + moduleLabel);
                writer.newLine();
                writer.newLine();
            }
        }
        logger.debug("Batch file generated successfully in output directory");
    }

    public List<String> getFormList() throws IOException {
        File sourceDir = new File(SOURCE_DIR);
        if (!sourceDir.exists()) {
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.list(Paths.get(SOURCE_DIR))) {
            return stream
                    .filter(path -> path.toString().endsWith(".yml"))
                    .map(path -> path.getFileName().toString())
                    .map(fileName -> fileName.substring(0, fileName.length() - 4))
                    .collect(Collectors.toList());
        }
    }

    public Form getForm(String name) throws IOException {
        String fileName = name + ".yml";
        File file = new File(SOURCE_DIR, fileName);
        if (!file.exists()) {
            return null;
        }

        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(inputStream);
            Form form = new Form();
            form.setName((String) data.get("name"));
            
            // 安全处理 variables
            Object variablesObj = data.get("variables");
            List<Form.Variable> variables = new ArrayList<>();
            if (variablesObj instanceof List<?>) {
                for (Object item : (List<?>) variablesObj) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> variableMap = (Map<String, String>) item;
                        Form.Variable variable = new Form.Variable();
                        variable.setNotes(variableMap.get("notes"));
                        variable.setKey(variableMap.get("key"));
                        variable.setValue(variableMap.get("value"));
                        variables.add(variable);
                    }
                }
            }
            form.setVariables(variables);

            // 安全处理 commands
            Object commandsObj = data.get("commands");
            List<Form.Command> commands = new ArrayList<>();
            if (commandsObj instanceof List<?>) {
                for (Object item : (List<?>) commandsObj) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> commandMap = (Map<String, Object>) item;
                        Form.Command command = new Form.Command();
                        command.setNotes((String) commandMap.get("notes"));
                        command.setContent((String) commandMap.get("content"));
                        
                        // 安全处理 active 状态
                        Object activeObj = commandMap.get("active");
                        command.setActive(activeObj instanceof Boolean ? (Boolean) activeObj : false);
                        
                        commands.add(command);
                    }
                }
            }
            form.setCommands(commands);
            return form;
        }
    }
}
