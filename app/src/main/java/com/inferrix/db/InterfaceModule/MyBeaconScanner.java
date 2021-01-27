package com.inferrix.db.InterfaceModule;


import com.inferrix.db.pogoClasses.BeconDeviceClass;

import java.util.ArrayList;

public interface MyBeaconScanner {
    void onBeaconFound(ArrayList<BeconDeviceClass> byteQueue);
    void noBeaconFound();
}
