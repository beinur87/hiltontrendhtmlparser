package BACnetObjects;

import TrendObjects.TrendAI;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

import java.awt.color.ICC_ColorSpace;
import java.util.List;

public class BACNetAIObjects {
    private LocalDevice localDevice;
    private List<TrendAI> rowDataList;

    public BACNetAIObjects(LocalDevice localDevice, List<TrendAI> rowDataList) throws BACnetServiceException {
        this.localDevice = localDevice;
        this.rowDataList = rowDataList;
        createObjects();
    }

    public void createObjects() throws BACnetServiceException {
        int instanceCounter = 1;
        for (TrendAI ai : rowDataList) {
            // Create a test analog value object
            ObjectIdentifier objectId = new ObjectIdentifier(ObjectType.analogValue, instanceCounter);
            BACnetObject object = new BACnetObject(localDevice, objectId);
            ValueSource valueSource = new ValueSource();
            //  localDevice.addObject(object);

// Set some common analog value properties
            object.writeProperty(valueSource, PropertyIdentifier.objectName, new CharacterString(ai.getOutstation() + "_" + ai.getItem() + "_" + instanceCounter));
            object.writeProperty(valueSource, PropertyIdentifier.description, new CharacterString(ai.getLabel()));
            object.writeProperty(valueSource, PropertyIdentifier.presentValue, new Real(Float.parseFloat(ai.getValue())));
            if (ai.getAlarm().equals("Out of Limits")) {
                object.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(true, false, false, false));
                object.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.offnormal);
            } else {
                object.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(false, false, false, false));
                object.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.normal);
            }
            object.writeProperty(valueSource, PropertyIdentifier.outOfService, com.serotonin.bacnet4j.type.primitive.Boolean.valueOf(false));
            localDevice.addObject(object);
            instanceCounter++;
        }
    }

    public void updateObjects(List<TrendAI> dataList) throws BACnetServiceException {
        for (BACnetObject obj : localDevice.getLocalObjects()) {
            if (obj.getObjectName().contains("_")) {
                String objName = obj.getObjectName();
                String os = objName.substring(0, objName.indexOf('_'));
                String item = objName.substring(objName.indexOf('_') + 1, objName.lastIndexOf('_'));
                for (TrendAI ai : dataList) {
                    if (ai.getOutstation().equals(os) && ai.getItem().equals(item)) {
                        Real trendAIvalue = new Real(Float.parseFloat(ai.getValue()));
                        if (!trendAIvalue.equals(obj.readProperty(PropertyIdentifier.presentValue)))
                            obj.writeProperty(new ValueSource(), PropertyIdentifier.presentValue, trendAIvalue);
                        System.out.println("Updated os=" + os + " item=" + item);
                        break;
                    }
                }

            }
        }

    }


}
