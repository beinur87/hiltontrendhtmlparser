package TrendObjects.Parser;

import TrendObjects.TrendAI;
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

public class TrendParserAI {
    private List<TrendAI> rowDataList;

    public List<TrendAI> scan() {
        rowDataList = new ArrayList<>();
        for (int i=111;i<=121;i++) {
            for (int j=1;j<=30;j=j+10){
            String url = "http://192.168.254."+i+"/S.htm?ovrideStart="+j;
            parseHTMLTable(url);
            }
        }
        writeCSV();
        return rowDataList;
    }

    public void writeCSV() {
        try {
            FileWriter writer = new FileWriter("ai.csv");
            writer.write("Outstation,Item,Label,Value,Units,Alarm,\n");
            for (TrendAI rowData : rowDataList) {
                writer.write( rowData.getOutstation() + "," + rowData.getItem() + "," + rowData.getLabel() + "," + rowData.getValue() + "," + rowData.getUnits() + "," + rowData.getAlarm() + "\n");
            }
            writer.close();
            System.out.println("CSV file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void parseHTMLTable(String url)  {
       // List<AlarmTableRowData> rowDataList = new ArrayList<>();
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
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
                    String units = cells.get(3).text();
                    String alarm = cells.get(4).text();
                    rowDataList.add(new TrendAI(outstation, item, label, value, units, alarm));
                }catch (IndexOutOfBoundsException e){
                    System.out.println("Index out of bounds for " + url + "on row " + i);
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
                    String units = cells.get(3).text();
                    String alarm = cells.get(4).text();
                    rowDataList.add(new TrendAI(outstation, item, label, value, units, alarm));
                }catch (IndexOutOfBoundsException e){
                    System.out.println("Index out of bounds for "+url);
                }

            }
        }

        } catch (IOException e) {
            System.out.println("Could not access page: " + url);
        }
    }


    public String extractIpAddress(String url) {
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



}
