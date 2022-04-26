package ru.sir.ymodem;

import android.util.Log;
import android.widget.Toast;

import com.van.uart.LastError;
import com.van.uart.UartManager;
import com.wl.wlflatproject.MUtils.Constants;
import com.wl.wlflatproject.MUtils.PostEventBus;
import com.wl.wlflatproject.MUtils.SerialPortUtil;

import java.io.DataInputStream;
import java.io.IOException;

import android_serialport_api.SerialPort;


/**
 * This is core Modem class supporting XModem (and some extensions XModem-1K, XModem-CRC), and YModem.<br/>
 * YModem support is limited (currently block 0 is ignored).<br/>
 * <br/>
 * Created by Anton Sirotinkin (aesirot@mail.ru), Moscow 2014 <br/>
 * I hope you will find this program useful.<br/>
 * You are free to use/modify the code for any purpose, but please leave a reference to me.<br/>
 */
class Modem {
    private UartManager mUartManager;
    /* Protocol characters used */
    protected static final byte SOH = 0x01; /* Start Of Header */
    protected static final byte STX = 0x02; /* Start Of Text (used like SOH but means 1024 block size) */
    protected static final byte EOT = 0x04; /* End Of Transmission */
    protected static final byte ACK = 0x06; /* ACKnowlege */
    protected static final byte NAK = 0x15; /* Negative AcKnowlege */
    protected static final byte CAN = 0x18; /* CANcel character */

    protected static final byte CPMEOF = 0x1A;
    protected static final byte ST_C = 'C';

    protected static final int MAXERRORS = 10;

    protected static final int BLOCK_TIMEOUT = 1000;
    protected static final int REQUEST_TIMEOUT = 3000;
    protected static final int WAIT_FOR_RECEIVER_TIMEOUT = 60_000;
    protected static final int SEND_BLOCK_TIMEOUT = 10_000;

    protected Modem(UartManager manager) {
        mUartManager = manager;
    }
    protected Modem() {
    }


    /**
     * Wait for receiver request for transmission
     *
     * @param timer
     * @return TRUE if receiver requested CRC-16 checksum, FALSE if 8bit checksum
     * @throws IOException
     */
    protected boolean waitReceiverRequest(Timer timer,SerialPortUtil serialPortUtil) throws IOException {
        Log.e("固件升级---","准备正式发数据了");
        int character;
        while (true) {
            try {
                character = readByte(timer,serialPortUtil);
                if (character == NAK)
                    return false;
                if (character == ST_C) {
                    return true;
                }
            } catch (TimeoutException e) {
                Log.e("固件升级---","超时");
                throw new IOException("固件升级---超时");
            }
        }
    }

    protected void sendDataBlocks(DataInputStream dataStream, int blockNumber, CRC crc, byte[] block,SerialPortUtil serialPortUtil) throws IOException {
        Log.e("固件升级---","正式开始传数据");
        int dataLength;
        while ((dataLength = dataStream.read(block)) != -1) {
            Log.e("固件升级---","写了："+dataLength);
            sendBlock(blockNumber++, block, dataLength, crc,serialPortUtil);
        }
        Log.e("固件升级---","数据传输完成");
    }

    protected void sendEOT(SerialPortUtil serialPortUtil) throws IOException {
        int errorCount = 0;
        Timer timer = new Timer(BLOCK_TIMEOUT);
        int character;
        while (errorCount < 10) {
            serialPortUtil.sendDate(new byte[EOT]);
            try {
                character = readByte(timer.start(),serialPortUtil);
                if (character == ACK) {
//                    PostEventBus.post("pro_" + (Constants.sCurrentPro++));
                    return;
                } else if (character == CAN) {
                    Log.e("固件升级---","传播中断");
                    throw new IOException("固件升级---传播中断");
                }
            } catch (TimeoutException ignored) {
            }
            errorCount++;
        }
    }

    protected void sendBlock(int blockNumber, byte[] block, int dataLength, CRC crc, SerialPortUtil serialPortUtil) throws IOException {
        int errorCount;
        int character;
        Timer timer = new Timer(SEND_BLOCK_TIMEOUT);

        if (dataLength < block.length) {
            block[dataLength] = CPMEOF;
        }
        errorCount = 0;

        while (errorCount < MAXERRORS) {
            timer.start();

            if (block.length == 1024)
                serialPortUtil.sendDate(new byte[]{STX});
            else //128
                serialPortUtil.sendDate(new byte[]{SOH});

            serialPortUtil.sendDate(new byte[]{(byte) blockNumber});
            serialPortUtil.sendDate(new byte[]{((byte) ~blockNumber)});
            serialPortUtil.sendDate(block);
            writeCRC(block, crc,serialPortUtil);
            while (true) {
                try {
                    character = readByte(timer,serialPortUtil);
                    if (character == ACK) {
//                        PostEventBus.post("pro_" + (Constants.sCurrentPro++));
                        return;
                    } else if (character == NAK) {
                        errorCount++;
                        break;
                    } else if (character == CAN) {
                        Log.e("固件升级---","传播中断");
                        throw new IOException("固件升级---传播中断");
                    }
                } catch (TimeoutException e) {
                    errorCount++;
                    break;
                }
            }

        }
        Log.e("固件升级","报错次数太多不传了");
        throw new IOException("报错次数太多不传了");
    }

    private void writeCRC(byte[] block, CRC crc,SerialPortUtil serialPortUtil) throws IOException {
        byte[] crcBytes = new byte[crc.getCRCLength()];
        long crcValue = crc.calcCRC(block);
        for (int i = 0; i < crc.getCRCLength(); i++) {
            crcBytes[crc.getCRCLength() - i - 1] = (byte) ((crcValue >> (8 * i)) & 0xFF);
        }
        serialPortUtil.sendDate(crcBytes);
    }


    protected void sendByte(byte b) throws IOException {
        write(new byte[]{b});
    }

    private void shortSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            try {
                interruptTransmission();
            } catch (IOException ignore) {
            }
            throw new RuntimeException("Transmission was interrupted", e);
        }
    }

    /**
     * send CAN to interrupt seance
     *
     * @throws IOException
     */
    protected void interruptTransmission() throws IOException {
        sendByte(CAN);
        sendByte(CAN);
    }


    private char readByte(Timer timer, SerialPortUtil serialPortUtil) throws IOException, TimeoutException {
        while (true) {
            char[] buf = new char[1];
            try {
                int read = serialPortUtil.inputStream.read(buf, 0, 1);
//                int read = mUartManager.read(buf, 1, 100, 20);
                if (read > 0) {
                    return buf[0];
                }
            } catch (Exception lastError) {
                Log.e("固件升级报错--", lastError.toString());
                lastError.printStackTrace();
            }
            if (timer.isExpired()) {
                throw new TimeoutException();
            }
            shortSleep();
        }
    }

    public void write(byte[] b) {
        try {
            mUartManager.write(b, b.length);
        } catch (LastError lastError) {
            lastError.printStackTrace();
        }
    }
}

