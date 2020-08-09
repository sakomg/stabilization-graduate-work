package com.saska.stabsputnikapp.receivingdata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommunicateFile {

    public static final String FILE = "src/main/resources/txt/SerialReceive.txt";

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public StringBuilder fileReader() throws IOException {
        FileReader rFile = new FileReader(FILE);
        Scanner scan = new Scanner(rFile);
        StringBuilder fullFile = new StringBuilder();
        while (scan.hasNextInt()) {
            fullFile.append(scan.nextInt());
        }
        rFile.close();
        return fullFile;
    }

    public String readFile() throws IOException {
        return Files.lines(Paths.get(FILE)).reduce("", (a, b) -> a + "\n" + b).trim();
    }

    public List<Double> reader() throws FileNotFoundException {
        File file = new File(FILE);
        Scanner scanner = new Scanner(file);
        List<Double> doublesInput = new ArrayList<>();
        while (scanner.hasNext()) {
            if (scanner.hasNextDouble()) {
                doublesInput.add(scanner.nextDouble());
            } else {
                scanner.next();
            }
        }
        return doublesInput;
    }

    public void fileWriter(String buffer, String path) throws IOException {
        FileWriter wFile = new FileWriter(path, true);
        wFile.write(buffer);
        wFile.close();
    }

    public void logFileWriter(String buffer, String path) {
        try {
            FileWriter fw = new FileWriter(path, true);
            PrintWriter out = new PrintWriter(fw);
            DateFormat dateF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time = dateF.format(cal.getTime());
            out.print(time + ": ");
            fw.write(buffer.toUpperCase() + "\n");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}