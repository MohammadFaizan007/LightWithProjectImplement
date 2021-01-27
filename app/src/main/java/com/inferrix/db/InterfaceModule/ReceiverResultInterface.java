package com.inferrix.db.InterfaceModule;


import com.inferrix.db.EncodeDecodeModule.ByteQueue;

public interface ReceiverResultInterface {

    void onScanSuccess(int successCode, ByteQueue byteQueue);
    void onScanFailed(int errorCode);


}
