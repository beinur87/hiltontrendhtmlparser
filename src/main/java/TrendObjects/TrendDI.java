package TrendObjects;

public class TrendDI {
        private final String outstation;
        private final String item;
        private final String label;
        private final String state;
        private final String alarm;
    public TrendDI(String outstation, String item, String label, String state, String alarm)  {
        this.outstation = outstation;
        this.item = item;
        this.label = label;
        this.state = state;
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

    public String getState() {
        return state;
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
                ", state='" + state + '\'' +
                ", alarm='" + alarm + '\'' +
                '}';
    }

}
