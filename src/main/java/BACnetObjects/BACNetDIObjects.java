package BACnetObjects;

import TrendObjects.TrendAI;
import TrendObjects.TrendDI;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

import java.util.List;

public class BACNetDIObjects {
    private LocalDevice localDevice;
    private List<TrendDI> rowDataList;

    public BACNetDIObjects(LocalDevice localDevice, List<TrendDI> rowDataList) throws BACnetServiceException, BACnetErrorException {
        this.localDevice = localDevice;
        this.rowDataList = rowDataList;
        createObjects();
    }

    public void createObjects() throws BACnetServiceException, BACnetErrorException {
        int instanceCounter = 1;
        for (TrendDI di : rowDataList) {
            // Create a test analog value object
            ObjectIdentifier objectId = new ObjectIdentifier(ObjectType.binaryInput, instanceCounter);
            BACnetObject object = new BACnetObject(localDevice, objectId);
            ValueSource valueSource = new ValueSource();
            //  localDevice.addObject(object);

// Set some common analog value properties
            boolean b = di.getState().equals("On");
            object.writeProperty(valueSource, PropertyIdentifier.objectName, new CharacterString(di.getOutstation() + "_" + di.getItem() + "_" + instanceCounter));
            object.writeProperty(valueSource, PropertyIdentifier.description, new CharacterString(di.getLabel()));
            object.writeProperty(valueSource, PropertyIdentifier.presentValue, b ? BinaryPV.active : BinaryPV.inactive);
            if (b == false) {
                object.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(false, false, false, false));
                object.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.normal);
            } else {
                object.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(true, false, false, false));
                object.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.fault);
            }

            object.writeProperty(valueSource, PropertyIdentifier.outOfService, com.serotonin.bacnet4j.type.primitive.Boolean.valueOf(false));
            object.writeProperty(valueSource, PropertyIdentifier.activeText, new CharacterString("On"));
            object.writeProperty(valueSource, PropertyIdentifier.inactiveText, new CharacterString("Off"));


            localDevice.addObject(object);
            instanceCounter++;
        }
    }

    public void updateObjects(List<TrendDI> dataList) throws BACnetServiceException {
        for (BACnetObject obj : localDevice.getLocalObjects()) {
            if (obj.getObjectName().contains("_")) {
                String objName = obj.getObjectName();
                String os = objName.substring(0, objName.indexOf('_'));
                String item = objName.substring(objName.indexOf('_') + 1, objName.lastIndexOf('_'));
                for (TrendDI di : dataList) {
                    if (di.getOutstation().equals(os) && di.getItem().equals(item)) {
                        boolean DIb = di.getState().equals("On");
                        if (!obj.readProperty(PropertyIdentifier.presentValue).equals(Boolean.valueOf(DIb))) {
                            ValueSource valueSource = new ValueSource();
                            obj.writeProperty(valueSource, PropertyIdentifier.presentValue, DIb ? BinaryPV.active : BinaryPV.inactive);
                            if (!DIb) {
                                obj.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(false, false, false, false));
                                obj.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.normal);
                            } else {
                                obj.writeProperty(valueSource, PropertyIdentifier.statusFlags, new StatusFlags(true, false, false, false));
                                obj.writeProperty(valueSource, PropertyIdentifier.eventState, EventState.fault);
                            }
                        }
                        System.out.println("Updated os=" + os + " item=" + item);
                        break;
                    }
                }

            }
        }

    }


}
