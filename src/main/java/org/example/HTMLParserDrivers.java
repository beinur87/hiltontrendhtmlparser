package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParserDrivers {
    public static List<DriverRowData> rowDataList;


    public static void main(String[] args) throws IOException {
        rowDataList = new ArrayList<>();
        for (int i=111;i<=121;i++) {
            try{
                for (int j=0;j<=200;j=j+10){
                String url = "http://192.168.254."+i+"/D.htm?ovrideStart="+j;
                parseHTMLTable(url);
                    if (i<10) i = 1;
                }
            }
            catch (ConnectException e){
                System.out.println("Failed to connect to http://192.168.254."+i);
            }
        }
        writeCSV();




    }

    public static void writeCSV() {
        try {
            FileWriter writer = new FileWriter("drivers.csv");
            writer.write("Outstation,Item,Label,Value,Module Status,Alarm,\n");
            for (DriverRowData rowData : rowDataList) {
                writer.write( rowData.getOutstation() + "," + rowData.getItem() + "," + rowData.getLabel() + "," + rowData.getValue()+","+rowData.getStatus() + "," + rowData.getAlarm() + "\n");
            }
            writer.close();
            System.out.println("CSV file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void parseHTMLTable(String url) throws IOException {
       // List<AlarmTableRowData> rowDataList = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Element table = doc.select("td[id=maindata]").first();
        // Element table = doc.select("table").first();
        Elements rows = table.select("tr");
        if (rows.size()>=9) {
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                String outstation = String.valueOf (Integer.valueOf(extractIpAddress(url))-100);
                try {
                    String item = cells.get(0).text();
                    String label = cells.get(1).text();
                    String value = cells.get(2).text();
                    String status = cells.get(3).text();
                    String alarm = cells.get(4).text();

                    rowDataList.add(new DriverRowData(outstation, item, label,value, status, alarm));
                }catch (IndexOutOfBoundsException e){
                System.out.println("Index out of bounds for "+url);
            }
            }
        }else {
            for (int i = 1; i < rows.size()-1; i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                String outstation = String.valueOf (Integer.valueOf(extractIpAddress(url))-100);
                try {
                    String item = cells.get(0).text();
                    String label = cells.get(1).text();
                    String value = cells.get(2).text();
                    String status = cells.get(3).text();
                    String alarm = cells.get(4).text();
                    rowDataList.add(new DriverRowData(outstation, item, label, value, status, alarm));
                }catch (IndexOutOfBoundsException e){
                System.out.println("Index out of bounds for "+url);
            }
            }
        }
    }


    public static String extractIpAddress(String url) {
        String regex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String s = matcher.group();
            s = s.substring(s.lastIndexOf('.')+1);
            return s;
        }
        return null; // no IP address found
    }


    public static class DriverRowData {
        private final String outstation;
        private final String item;
        private final String label;
        private final String value;
        private final String status;
        private final String alarm;


        public DriverRowData(String outstation, String item, String label, String value, String status, String alarm)  {
            this.outstation = outstation;
            this.item = item;
            this.label = label;
            this.value = value;
            this.status = status;
            this.alarm = alarm;
        }

        public String getOutstation() {
            return outstation;
        }

        public String getItem() {
            return item;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
        public String getStatus() {
            return status;
        }

        public String getAlarm() {
            return alarm;
        }

        @Override
        public String toString() {
            return "DIRowData{" +
                    "outstation='" + outstation + '\'' +
                    ", item='" + item + '\'' +
                    ", label='" + label + '\'' +
                    ", value='" + value + '\'' +
                    ", status='" + status + '\'' +
                    ", alarm='" + alarm + '\'' +
                    '}';
        }
    }
}
