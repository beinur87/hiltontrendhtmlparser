package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Time;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParserAlarmList {
    public static List<AlarmTableRowData> rowDataList;

    public static void main(String[] args) throws IOException {
        rowDataList = new ArrayList<>();
        for (int i=111;i<=121;i++) {
            try{
            String url = "http://192.168.254."+i+"/alarms.htm?page=1&limit=10000";
            parseHTMLTable(url);}
            catch (ConnectException e){
                System.out.println("Failed to connect to http://192.168.254."+i);
            }
        }
        sortRowDataListByTime();
       // removeDuplicateModulesRef();
       // removeDuplicateModulesRefPerOutstation();
        writeCSV();
    }

    public static void writeCSV() {
        try {
            FileWriter writer = new FileWriter("alarms.csv");
            writer.write("Time, Outstation,ModulesRef,ModuleLabel,Type,Value,Transition,CurrentState\n");
            for (AlarmTableRowData rowData : rowDataList) {
                writer.write( rowData.getTime() + "," + rowData.getOutstation() + "," + rowData.getModulesRef() + "," + rowData.getModuleLabel() + "," + rowData.getType() + "," + rowData.getValue()  + "," + rowData.getTransition() + "," + rowData.getCurrentState() + "\n");
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
                    String modulesRef = cells.get(0).text();
                    String moduleLabel = cells.get(1).text();
                    String type = cells.get(2).text();
                    String value = cells.get(3).text();
                    String time = cells.get(4).text();
                    String transition = cells.get(5).text();
                    String currentState = cells.get(6).text();
                    rowDataList.add(new AlarmTableRowData(outstation,modulesRef,moduleLabel,type,value, time, transition, currentState));
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
                    String modulesRef = cells.get(0).text();
                    String moduleLabel = cells.get(1).text();
                    String type = cells.get(2).text();
                    String value = cells.get(3).text();
                    String time = cells.get(4).text();
                    String transition = cells.get(5).text();
                    String currentState = cells.get(6).text();
                    rowDataList.add(new AlarmTableRowData(outstation, modulesRef, moduleLabel, type, value, time, transition, currentState));
                }catch (IndexOutOfBoundsException e){
                System.out.println("Index out of bounds for "+url);
            }
            }
        }
    }

    public static void sortRowDataListByTime() {
        Collections.sort(rowDataList, new Comparator<AlarmTableRowData>() {
            @Override
            public int compare(AlarmTableRowData o1, AlarmTableRowData o2) {
                return o1.time.compareTo(o2.time);
            }
        });
    }
    public static void removeDuplicateModulesRefPerOutstation() {
        Map<String, Set<String>> uniqueModulesRefPerOutstation = new HashMap<>();
        for (int i = rowDataList.size() - 1; i >= 0; i--) {
            AlarmTableRowData data = rowDataList.get(i);
            String outstation = data.outstation;
            String modulesRef = data.modulesRef;
            if (uniqueModulesRefPerOutstation.containsKey(outstation)) {
                Set<String> uniqueModulesRef = uniqueModulesRefPerOutstation.get(outstation);
                if (uniqueModulesRef.contains(modulesRef)) {
                    rowDataList.remove(i);
                } else {
                    uniqueModulesRef.add(modulesRef);
                }
            } else {
                Set<String> uniqueModulesRef = new HashSet<>();
                uniqueModulesRef.add(modulesRef);
                uniqueModulesRefPerOutstation.put(outstation, uniqueModulesRef);
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


    public static class AlarmTableRowData {
        private final String outstation;
        private final String modulesRef;
        private final String moduleLabel;
        private final String type;
        private final String value;
        private final String time;
        private final String transition;
        private final String currentState;

        public AlarmTableRowData(String outstation, String modulesRef, String moduleLabel, String type, String value, String time, String transition, String currentState)  {
            this.outstation = outstation;
            this.modulesRef = modulesRef;
            this.moduleLabel = moduleLabel;
            this.type = type;
            this.value = value;
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                Date date = inputFormat.parse(time);
                this.time = outputFormat.format(date);
            } catch (ParseException e) {
                System.out.println("Can't parse "+ time);
                throw new RuntimeException(e);

            }
            this.transition = transition;
            this.currentState = currentState;
        }

        public String getOutstation() {
            return outstation;
        }

        public String getModulesRef() {
            return modulesRef;
        }

        public String getModuleLabel() {
            return moduleLabel;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getTime() {
            return time;
        }

        public String getTransition() {
            return transition;
        }

        public String getCurrentState() {
            return currentState;
        }

        @Override
        public String toString() {
            return "AlarmTableRowData{" +
                    "time='" + time + '\'' +
                    "outstation='" + outstation + '\'' +
                    ", modulesRef='" + modulesRef + '\'' +
                    ", moduleLabel='" + moduleLabel + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", transition='" + transition + '\'' +
                    ", currentState='" + currentState + '\'' +
                    '}';
        }
    }
}
