package com.example.raveh.rscontrol;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Raveh on 28/10/2014.
 */
public class RsProtocol {

    private int state = 0;
    private byte pingValue = 0;

    public void connect(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac notif 9a66fb0f
        service.sendDescriptor(characteristics.get(3).get(15));
        //charac notif 9a66fb0e
        service.sendDescriptor(characteristics.get(3).get(14));
        //charac notif 9a66fb1b
        service.sendDescriptor(characteristics.get(3).get(27));
        //charac notif 9a66fb1c
        service.sendDescriptor(characteristics.get(3).get(28));
        //charac notif 9a66fd23
        service.sendDescriptor(characteristics.get(5).get(1));
        //charac notif 9a66fd53
        service.sendDescriptor(characteristics.get(6).get(1));

        //charac send 9a66fa0b
        final BluetoothGattCharacteristic infoChar = characteristics.get(2).get(11);
        UUID infoId = infoChar.getUuid();
        byte[] value = {(byte)0x04,(byte)0x01,(byte)0x00,(byte)0x04,
                        (byte)0x01,(byte)0x00,(byte)0x32,(byte)0x30,
                        (byte)0x31,(byte)0x34,(byte)0x2D,(byte)0x31,
                        (byte)0x30,(byte)0x2D,(byte)0x32,(byte)0x38, (byte)0x00};
        infoChar.setValue(value);
        service.sendCharacteristic(infoChar);
        state = 1;
    }

    void sendConfig(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa1e
        byte[] valueTmp ={(byte)0x01,(byte)state,(byte)state};
        final BluetoothGattCharacteristic magicChar = characteristics.get(2).get(30);
        magicChar.setValue(valueTmp);
        service.sendCharacteristic(magicChar);

        if (state == 1)
        {
            //charac send 9a66fa0b
            //04:02:00:04:02:00:54:31:35:35:33:35:39:2b:30:31:30:30:00
            byte[] value = {(byte)0x04,(byte)0x02,(byte)0x00,(byte)0x04,
                            (byte)0x02,(byte)0x00,(byte)0x54,(byte)0x31,
                            (byte)0x35,(byte)0x35,(byte)0x33,(byte)0x35,
                            (byte)0x39,(byte)0x2B,(byte)0x30,(byte)0x31,
                            (byte)0x30,(byte)0x30,(byte)0x00};
            final BluetoothGattCharacteristic infoChar = characteristics.get(2).get(11);
            infoChar.setValue(value);
            service.sendCharacteristic(infoChar);
        }
        else if (state == 2)
        {
            //charac send 9a66fa0b
            //04:03:00:02:00:00
            byte[] value = {(byte)0x04,(byte)0x03,(byte)0x00,(byte)0x02,
                            (byte)0x00,(byte)0x00};
            final BluetoothGattCharacteristic infoChar = characteristics.get(2).get(11);
            infoChar.setValue(value);
            service.sendCharacteristic(infoChar);
        }
        else if (state == 14)
        {
            //charac send 9a66fa0b
            //04:04:00:04:00:00
            byte[] value = {(byte)0x04,(byte)0x04,(byte)0x00,(byte)0x04,
                            (byte)0x00,(byte)0x00};
            final BluetoothGattCharacteristic infoChar = characteristics.get(2).get(11);
            infoChar.setValue(value);
            service.sendCharacteristic(infoChar);
        }
        state++;
    }

    public void takeOff(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:05:02:00:01:00
        byte[] value = {(byte)0x04,(byte)0x05,(byte)0x02,(byte)0x00,
                        (byte)0x01,(byte)0x00};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void land(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x07,(byte)0x02,(byte)0x00,
                (byte)0x03,(byte)0x00};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x18, (byte) 0x18};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);

        service.sendCharacteristic(takeOffChar);
    }

    public void button1(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x46,
                (byte)0x00,(byte)0x04, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x04, (byte) 0x00};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button2(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x16, (byte) 0x16};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button3(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x17, (byte) 0x17};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button4(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x46,
                (byte)0x00,(byte)0x04, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x04, (byte) 0x00};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button5(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x19, (byte) 0x19};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button6(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x1A, (byte) 0x1A};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void button7(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        //charac send 9a66fa0b
        //04:07:02:00:03:00
        byte[] value = {(byte)0x04,(byte)0x00,(byte)0x52,(byte)0x7C,
                (byte)0x00,(byte)0x01, (byte) 0x1B, (byte) 0x1B};
        final BluetoothGattCharacteristic takeOffChar = characteristics.get(2).get(11);
        takeOffChar.setValue(value);
        service.sendCharacteristic(takeOffChar);
    }

    public void sendPing(BluetoothLeService service, ArrayList<ArrayList<BluetoothGattCharacteristic>> characteristics)
    {
        if (state >= 14) {
            //charac send 9a66fa0a
            //02:{00-FF}:02:00:02:00:00:00:00:00:00:00:00:00:00
            byte[] value = {(byte) 0x02, pingValue, (byte) 0x02, (byte) 0x00,
                    (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00};
            final BluetoothGattCharacteristic pingChar = characteristics.get(2).get(10);
            pingChar.setValue(value);
            service.sendCharacteristic(pingChar);
            pingValue++;
        }
    }
}
