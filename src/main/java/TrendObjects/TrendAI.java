package TrendObjects;

public class TrendAI {
        private final String outstation;
        private final String item;
        private final String label;
        private final String value;
        private final String units;
        private final String alarm;


        public TrendAI(String outstation, String item,String label, String value, String units, String alarm)  {
            this.outstation = outstation;
            this.item = item;
            this.label = label;
            this.value = value;
            this.units = units;
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

        public String getUnits() {
            return units;
        }

        public String getAlarm() {
            return alarm;
        }

        @Override
        public String toString() {
            return "DIRowData{" +
                    "outstation='" + outstation + '\'' +
                    ", item='" + item + '\'' +
                    ", label='" + value + '\'' +
                    ", state='" + units + '\'' +
                    ", alarm='" + alarm + '\'' +
                    '}';
        }

}
