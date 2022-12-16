package io.blockshake.ledger;

import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.util.Arrays;

public class LedgerManager {

    static final int MAX_USBFS_BUFFER_SIZE = 16384;

    public UsbManager usbManager;

    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndpointReadIn;
    private UsbEndpoint usbEndpointWriteOut;

    public LedgerManager(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    /**
     * Open a new usb connection with the specified device.
     *
     * @param identifier The id of the Ledger device.
     * @throws LedgerException If unable to open a connection with the device.
     */
    public void open(String identifier) throws LedgerException {
        UsbDevice device = usbManager.getDeviceList().get(identifier);
        if (device == null) {
            throw new LedgerException(0x60000, "Device with identifier not found.");
        }

        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            throw new LedgerException(0x60001, "Unable to open connection. Do you have permission?");
        }

        // Check if the device has a configuration
        if (device.getConfigurationCount() <= 0) {
            throw new LedgerException(0x60002, "No configurations found.");
        }

        UsbConfiguration configuration = device.getConfiguration(0);
        connection.setConfiguration(configuration);

        // Find the interface
        UsbInterface usbInterface = findInterface(device, 255);
        if (usbInterface == null) {
            throw new LedgerException(0x60003, "USB interface not found.");
        }

        // Claim the interface
        boolean claimed = connection.claimInterface(usbInterface, true);
        if (!claimed) {
            throw new LedgerException(0x60004, "Unable to claim USB interface.");
        }

        // Find the endpoints
        UsbEndpoint endpointIn = findEndpoint(device, 3, 0x80);
        UsbEndpoint endpointOut = findEndpoint(device, 3, 0x00);

        if (endpointIn == null || endpointOut == null ){
            throw new LedgerException(0x60005, "USB endpoints not found.");
        }

        this.device = device;
        this.connection = connection;
        this.usbInterface = usbInterface;
        this.usbEndpointReadIn = endpointIn;
        this.usbEndpointWriteOut = endpointOut;
    }

    /**
     * Close the current connection with the Ledger device.
     */
    public void close() {
        if (!isConnected()) {
            return;
        }

        // Release the interface
        usbEndpointReadIn = null;
        usbEndpointWriteOut = null;
        connection.releaseInterface(usbInterface);
        connection.close();
        usbInterface = null;
        connection = null;
        device = null;
    }

    /**
     * Gracefully reset the device and connection.
     */
    public void gracefullyReset() {
        if (connection != null){
            if (usbInterface != null) {
                connection.releaseInterface(usbInterface);
                usbInterface = null;
            }

            connection.close();
            connection = null;
        }

        device = null;
        usbEndpointReadIn = null;
        usbEndpointWriteOut = null;
    }

    public byte[] transferIn(int packetSize, int timeout) throws LedgerException {
        if (!isConnected()) {
            throw new LedgerException(0x60001, "Not connected.");
        }

        try {
            byte[] buffer = new byte[packetSize];
            int length = connection.bulkTransfer(usbEndpointReadIn, buffer, buffer.length, timeout);
            if (length < 0) {
                return new byte[0];
            }

            return Arrays.copyOfRange(buffer, 0, length);
        } catch (Exception ex) {
            throw new LedgerException(0x60006, "Error reading data from usb endpoint");
        }
    }

    public int transferOut(byte[] data, int timeout) throws LedgerException {
        if (!isConnected()) {
            throw new LedgerException(0x60001, "Not connected.");
        }

        try {
            return connection.bulkTransfer(usbEndpointWriteOut, data, data.length, timeout);
        } catch (Exception ex) {
            throw new LedgerException(0x60006, "Error writing data to usb endpoint");
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public UsbDevice getDevice() {
        return device;
    }

    public UsbDeviceConnection getConnection() {
        return connection;
    }

    public UsbInterface findInterface(UsbDevice device, int interfaceClass) {
        int interfaceCount = device.getInterfaceCount();
        for (int i=0; i < interfaceCount; i++) {
            UsbInterface usbInterface = device.getInterface(i);
            if (usbInterface.getInterfaceClass() == interfaceClass) {
                return usbInterface;
            }
        }

        return null;
    }

    public UsbEndpoint findEndpoint(UsbDevice device, int endpointNumber, int direction) {
        int interfaceCount = device.getInterfaceCount();
        for (int i=0; i < interfaceCount; i++) {
            UsbInterface usbInterface = device.getInterface(i);

            for (int j=0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                if (endpoint.getEndpointNumber() == endpointNumber
                        && endpoint.getDirection() == direction) {
                    return endpoint;
                }
            }
        }

        return null;
    }
}
