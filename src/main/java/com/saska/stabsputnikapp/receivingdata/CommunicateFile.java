package com.saska.stabsputnikapp.receivingdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CommunicateFile {
    public int getCount() {
        int count = 0;

        FileReader fr = null;
        BufferedReader br = null;
        PrintWriter writer = null;

        try {
            File f = new File( "src/main/resources/txtfiles/SerialReceive.txt");
            if (!f.exists()) {
                f.createNewFile();
                writer = new PrintWriter(new FileWriter(f));
                writer.print(0);
            }
            if (writer != null) writer.close();

            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String initial = br.readLine();
            System.out.println(initial);
            count = Integer.parseInt(initial);
        } catch (Exception e) {
            if (writer != null) writer.close();
        }

        if (br != null) {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        return count;
    }

    public void save(int count) throws IOException {
        FileWriter fw = null;
        PrintWriter pw = null;
        fw = new FileWriter( "src/main/resources/txtfiles/SerialReceive.txt");
        pw = new PrintWriter(fw);
        pw.print(count);

        if (pw != null) pw.close();
    }
}