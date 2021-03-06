package com.inferrix.db.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;


import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.inferrix.db.EncodeDecodeModule.ByteQueue;
import com.inferrix.db.InterfaceModule.AdvertiseResultInterface;
import com.inferrix.db.InterfaceModule.MyBeaconScanner;
import com.inferrix.db.R;
import com.inferrix.db.ServiceModule.AdvertiseTask;
import com.inferrix.db.ServiceModule.ScanningBeacon;
import com.inferrix.db.activity.AppHelper;
import com.inferrix.db.adapter.AddDeviceListAdapter;
import com.inferrix.db.pogoClasses.BeconDeviceClass;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.inferrix.db.EncodeDecodeModule.RxMethodType.LIGHT_INFO;


public class AddDeviceFragment extends Fragment implements MyBeaconScanner, AdvertiseResultInterface {
    @BindView(R.id.device_list)
    ListView deviceList;
    @BindView(R.id.refresh)
    Button refresh;
    AddDeviceListAdapter addDeviceListAdapter;
    ArrayAdapter<CharSequence> adapter;
    int movement = 150;
    ScanningBeacon scanningBeacon;
    boolean isAdvertisingFinished = false;
    AdvertiseTask advertiseTask;
    AnimatedProgress animatedProgress;
    String TAG = this.getClass().getSimpleName();
    Unbinder unbinder;
    Activity activity;
    Handler handler;
    String projectID;
    private Runnable runnable = () -> {
        if (animatedProgress != null) {
            animatedProgress.hideProgress();
        }

    };

    public AddDeviceFragment() {
        // Required empty public constructor
    }

    private void handlerProgressar() {
        animatedProgress.showProgress();
        handler = new Handler ();
        handler.postDelayed( runnable, 15 * 1000 );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        projectID = getArguments().getString("projectID");
        Log.e ("CHECK=======>",projectID);
        View view = inflater.inflate( R.layout.fragment_add_device, container, false );
        unbinder = ButterKnife.bind( this, view );
        activity = getActivity();

        addDeviceListAdapter = new AddDeviceListAdapter( activity, projectID);
        deviceList.setAdapter( addDeviceListAdapter );
        scanningBeacon = new ScanningBeacon( activity );
        scanningBeacon.setMyBeaconScanner( this );

        animatedProgress = new AnimatedProgress( activity );
        animatedProgress.setCancelable( false );

//        Toast.makeText(getActivity(),  PreferencesManager.getInstance(activity).getUniqueKey(), Toast.LENGTH_SHORT).show();

        ByteQueue byteQueue = new ByteQueue ();
        byteQueue.push( LIGHT_INFO );   //// Light Level Command method type
        byteQueue.pushU4B( 0x00 );
        byteQueue.push( 0x00 );      ////deviceDetail.getGroupId()   node id;
//        byteQueue.push( 0x01 );
//        byteQueue.push( 0x02 );
//        byteQueue.push( 0x03 );
//        byteQueue.push( 0x04 );
//        [ef,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
        advertiseTask = new AdvertiseTask( this, activity, 5 * 1000 );
        animatedProgress.setText( "Advertising" );
        advertiseTask.setByteQueue( byteQueue );
        advertiseTask.setSearchRequestCode( LIGHT_INFO );
        advertiseTask.startAdvertising();

        refresh.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addDeviceListAdapter.clearList();
                ByteQueue byteQueue1 = new ByteQueue();
                byteQueue1.push( LIGHT_INFO );
                byteQueue1.push( 0x00 );
                byteQueue1.push( 0x00 );
                animatedProgress.setText( "Refreshing" );
                advertiseTask.setByteQueue( byteQueue1 );
                advertiseTask.setAdvertiseTimeout( 2 * 1000 );
                advertiseTask.setSearchRequestCode( LIGHT_INFO );
                advertiseTask.startAdvertising();
            }
        } );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdvertisingFinished) {
            animatedProgress.setText( "Scanning Sensor" );
            scanningBeacon.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scanningBeacon.stop();
        if (handler != null)
            handler.removeCallbacks( runnable );
        unbinder.unbind();
    }

    @Override
    public void onSuccess(String message) {
        handlerProgressar();
        Log.e( TAG, "Advertising start" );

    }

    @Override
    public void onFailed(String errorMessage) {
        isAdvertisingFinished = true;
        scanningBeacon.start();
        animatedProgress.setText( "Scanning Sensor" );

    }

    @Override
    public void onStop() {
        scanningBeacon.stop();
        super.onStop();
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
        isAdvertisingFinished = true;
        animatedProgress.setText( "Scanning Sensor" );
        scanningBeacon.start();

    }

    @Override
    public void onBeaconFound(ArrayList<BeconDeviceClass> beaconList) {
        if (addDeviceListAdapter == null)
            addDeviceListAdapter = new AddDeviceListAdapter( activity,projectID );
        addDeviceListAdapter.setArrayList( beaconList );

    }

    @Override
    public void noBeaconFound() {
        Log.w( "AddDeviceFragment", "No Beacon founded" );
        if (!AppHelper.IS_TESTING)
            addDeviceListAdapter.clearList();
//        Toast.makeText(activity, "no beacon found", Toast.LENGTH_SHORT).show();

    }

}
