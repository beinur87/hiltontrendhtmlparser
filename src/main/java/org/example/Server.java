package org.example;

import BACnetObjects.BACNetAIObjects;
import BACnetObjects.BACNetDIObjects;
import TrendObjects.Parser.TrendParserAI;
import TrendObjects.Parser.TrendParserDI;
import TrendObjects.TrendAI;
import TrendObjects.TrendDI;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.sero.ByteQueue;
import com.serotonin.bacnet4j.service.Service.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Server {

    public static final int port = 0xBAC0;
    private static List<TrendAI> aiDataList;
    private static List<TrendDI> diDataList;
    private static final String OBJECT_NAME = "Beinur's BACnet Object";
    private static final String OBJECT_DESCRIPTION = "This is a test object";
    private static final EngineeringUnits ENGINEERING_UNITS = EngineeringUnits.percent;


    public static void main(String[] args) throws Throwable {
        TrendParserAI AIparser = new TrendParserAI();
        TrendParserDI DIparser = new TrendParserDI();

        //IpNetwork network = new IpNetwork("10.78.20.255", 0xBAC5);
        IpNetwork network = new IpNetworkBuilder()
                .withPort(port)
                .withLocalNetworkNumber(1)
               // .withLocalBindAddress("192.168.8.215")
                .withBroadcast("192.168.8.255",24)
                .withSubnet("255.255.255.0",24)
                .build();
        Transport transport = new DefaultTransport(network);
        transport.setTimeout(500000);
        transport.setSegTimeout(15000);

        int localDeviceID = 10000 + 1987;//(int) ( Math.random() * 10000);
        LocalDevice localDevice = new LocalDevice(localDeviceID, transport);
        localDevice.initialize();

        System.out.println("Local device is running with device id " + localDeviceID);
        System.out.println(network.getLocalNetworkNumber());

        aiDataList = AIparser.scan();
        diDataList = DIparser.scan();

        BACNetAIObjects AIobj = new BACNetAIObjects(localDevice, aiDataList);
        BACNetDIObjects DIobj = new BACNetDIObjects(localDevice, diDataList);

        Thread updateAI = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {

                    try {
                        aiDataList = AIparser.scan();
                        AIobj.updateObjects(aiDataList);

                    } catch (BACnetServiceException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        }, "BACnet AI update");

        Thread updateDI = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {

                    try {
                        diDataList = DIparser.scan();
                        DIobj.updateObjects(diDataList);

                    } catch (BACnetServiceException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        }, "BACnet DI update");


        updateAI.setDaemon(true);
        updateAI.start();

        updateDI.setDaemon(true);
        updateDI.start();

        //localDevice.initialize();

        System.in.read();
        localDevice.terminate();
    }

}