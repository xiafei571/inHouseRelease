package com.example.formapp.service;

import com.example.formapp.model.Form;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FormService {
    private static final String OUTPUT_DIR = "output";

    public void saveForm(Form form) throws IOException {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        String fileName = form.getName() + ".txt";
        try (FileWriter writer = new FileWriter(new File(outputDir, fileName))) {
            writer.write(form.getContent());
        }
    }

    public List<String> getFormList() throws IOException {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            return new ArrayList<>();
        }

        return Files.list(Paths.get(OUTPUT_DIR))
                .filter(path -> path.toString().endsWith(".txt"))
                .map(path -> path.getFileName().toString())
                .map(fileName -> fileName.substring(0, fileName.length() - 4))
                .toList();
    }

    public Form getForm(String name) throws IOException {
        String fileName = name + ".txt";
        File file = new File(OUTPUT_DIR, fileName);
        if (!file.exists()) {
            return null;
        }

        String content = new String(Files.readAllBytes(file.toPath()));
        Form form = new Form();
        form.setName(name);
        form.setContent(content);
        return form;
    }
}
