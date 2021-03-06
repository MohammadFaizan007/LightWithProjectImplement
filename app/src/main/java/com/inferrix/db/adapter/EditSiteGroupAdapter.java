package com.inferrix.db.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.CustomProgress.CustomDialog.AnimatedProgress;
import com.inferrix.db.EncodeDecodeModule.ArrayUtilities;
import com.inferrix.db.EncodeDecodeModule.ByteQueue;
import com.inferrix.db.EncodeDecodeModule.RxMethodType;
import com.inferrix.db.InterfaceModule.AdvertiseResultInterface;
import com.inferrix.db.InterfaceModule.ReceiverResultInterface;
import com.inferrix.db.R;
import com.inferrix.db.ServiceModule.AdvertiseTask;
import com.inferrix.db.ServiceModule.ScannerTask;
import com.inferrix.db.databaseModules.DatabaseConstant;
import com.inferrix.db.fragment.EditGroupSiteFragment;
import com.inferrix.db.pogoClasses.SiteDeviceClass;
import com.inferrix.db.pogoClasses.SiteGroupDetailsClass;
import com.niftymodaldialogeffects.Effectstype;
import com.niftymodaldialogeffects.NiftyDialogBuilder;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.math.BigInteger;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.inferrix.db.EncodeDecodeModule.ArrayUtilities.bytesToHex;
import static com.inferrix.db.EncodeDecodeModule.RxMethodType.LIGHT_LEVEL_COMMAND;
import static com.inferrix.db.EncodeDecodeModule.TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE;
import static com.inferrix.db.EncodeDecodeModule.TxMethodType.LIGHT_STATE_COMMAND_RESPONSE;
import static com.inferrix.db.EncodeDecodeModule.TxMethodType.REMOVE_GROUP_RESPONSE;
import static com.inferrix.db.activity.AppHelper.sqlHelper;
import static com.inferrix.db.databaseModules.DatabaseConstant.COLUMN_DEVICE_PROGRESS;
import static com.inferrix.db.databaseModules.DatabaseConstant.COLUMN_DEVICE_STATUS;
import static com.inferrix.db.databaseModules.DatabaseConstant.COLUMN_GROUP_SITE_ID;


public class EditSiteGroupAdapter extends BaseAdapter implements ReceiverResultInterface, AdvertiseResultInterface {
    Activity activity;
    ArrayList<SiteDeviceClass> arrayList;
    ArrayList<String> filterarrayList;
    ProgressDialog progressDialog;
    ArrayList<String> selectedList;
    SiteGroupDetailsClass groupDetailsClass;
    String TAG=this.getClass().getSimpleName();
    ScannerTask scannerTask;
    AnimatedProgress animatedProgress;
    int selectedPosition = 0;
    EditGroupSiteFragment editGroupFragment;


    public EditSiteGroupAdapter(@NonNull Activity context, SiteGroupDetailsClass groupId, EditGroupSiteFragment editGroupFragment) {
        activity = context;
        arrayList = new ArrayList<>();
        selectedList=new ArrayList<>();
        filterarrayList = new ArrayList<>();
        this.groupDetailsClass=groupId;
        this.editGroupFragment=editGroupFragment;
        animatedProgress=new AnimatedProgress (activity);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
        scannerTask=new ScannerTask(activity,this);
        animatedProgress=new AnimatedProgress (activity);
        animatedProgress.setCancelable(false);

    }


    public void setArrayList(ArrayList<SiteDeviceClass> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public SiteDeviceClass getItem(int position) {
        if (arrayList.size() <= position)
            return null;
        return arrayList.get(position);
    }

    public void showDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customize_grouped_light_dialog);
        dialog.show();

    }
    void showDialog( int index)
    {
        final Dialog dialog = new Dialog(activity);
        SiteDeviceClass deviceDetail=arrayList.get(index);
        final int[] seekBarProgress = {deviceDetail.getDeviceDimming()};
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customize_grouped_light_dialog);

        TextView deviceName=dialog.findViewById(R.id.add_device_uid);
        SeekBar seekBar=dialog.findViewById(R.id.dimming_spinner);
        Button button=dialog.findViewById(R.id.save);

        JellyToggleButton jellyToggle=dialog.findViewById(R.id.status_switch);
        jellyToggle.setChecked(deviceDetail.getStatus());
        jellyToggle.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb)
            {

                boolean switchStatus= state != State.LEFT;
                if(deviceDetail.getStatus()==switchStatus)
                {
//                    Log.w("Advertise","state is same");
                    return;
                }
//                Log.w("DeviceID",deviceDetail.getDeviceUID()+"");

                AdvertiseTask advertiseTask;
                ByteQueue byteQueue=new ByteQueue();
                byteQueue.push(RxMethodType.LIGHT_STATE_COMMAND);       ////State Command
                byteQueue.pushU4B(deviceDetail.getDeviceUID());      ////  12 is static vale for Node id
//                Log.w(TAG,state+"");
                switch (state)
                {
                    case LEFT:

                        byteQueue.push(0x00);   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(false);

                        break;
                    case RIGHT:
//                        Log.w("SwitchStatus","Right");
                        byteQueue.push(0x01 );   //0x00 – OFF    0x01 – ON
//                        arrayList.get(position).setStatus(true);
                        break;
                    case LEFT_TO_RIGHT:

                        return;

                    case RIGHT_TO_LEFT:
                        return;

                }
                selectedPosition=index;
                advertiseTask=new AdvertiseTask(EditSiteGroupAdapter.this,activity,5*1000);
                advertiseTask.setByteQueue(byteQueue);
                advertiseTask.setSearchRequestCode(LIGHT_STATE_COMMAND_RESPONSE);
                advertiseTask.startAdvertising();
            }
        });
//
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                seekBarProgress[0] =i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        button.setOnClickListener(view -> {
            Log.w("IndividualLight",seekBarProgress[0]+"");
            ContentValues contentValues=new ContentValues();
            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
            contentValues.put(COLUMN_DEVICE_PROGRESS,seekBarProgress[0]);
            String hex = Integer.toHexString(seekBarProgress[0]);
            Log.w("IndividualLight",hex+" "+String.format("%02X", seekBarProgress[0]));
            AdvertiseTask advertiseTask;
            selectedPosition=index;

            ByteQueue byteQueue=new ByteQueue();
            byteQueue.push(LIGHT_LEVEL_COMMAND);   //// Light Level Command method type
            byteQueue.pushU4B(deviceDetail.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
            byteQueue.push(hex);    ////0x00-0x64
//            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
            advertiseTask=new AdvertiseTask(this,activity,5*1000);
            advertiseTask.setByteQueue(byteQueue);
            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
            advertiseTask.startAdvertising();

            dialog.dismiss();
        });
        deviceName.setText(deviceDetail.getDeviceName());

        seekBar.setProgress(deviceDetail.getDeviceDimming());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                seekBarProgress[0] =i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        button.setOnClickListener(view -> {
            Log.w("IndividualLight",seekBarProgress[0]+"");
            ContentValues contentValues=new ContentValues();
            arrayList.get(index).setDeviceDimming(seekBarProgress[0]);
            contentValues.put(COLUMN_DEVICE_PROGRESS,seekBarProgress[0]);
            String hex = Integer.toHexString(seekBarProgress[0]);
            Log.w("IndividualLight",hex+" "+String.format("%02X", seekBarProgress[0]));
            AdvertiseTask advertiseTask;
            selectedPosition=index;

            ByteQueue byteQueue=new ByteQueue();
            byteQueue.push(LIGHT_LEVEL_COMMAND);   //// Light Level Command method type
            byteQueue.pushU4B(deviceDetail.getDeviceUID());   ////deviceDetail.getGroupId()   node id;
            byteQueue.push(hex);    ////0x00-0x64
//            scannerTask.setRequestCode(TxMethodType.LIGHT_LEVEL_COMMAND_RESPONSE);
            advertiseTask=new AdvertiseTask(this,activity,5*1000);
            advertiseTask.setByteQueue(byteQueue);
            advertiseTask.setSearchRequestCode(LIGHT_LEVEL_COMMAND_RESPONSE);
            advertiseTask.startAdvertising();
            Log.w("IndividualLight", sqlHelper.updateDeviceNew(deviceDetail.getDeviceId (),contentValues)+"");
            dialog.dismiss();
        });
        deviceName.setText(deviceDetail.getDeviceName());

        dialog.show();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).
                    inflate(R.layout.edit_group_list_adapter, parent, false);
        }
            SiteDeviceClass deviceClass=arrayList.get(position);
            ViewHolder viewHolder=new ViewHolder(convertView);
            viewHolder.removeDevice.setText("Remove");
            viewHolder.lightDetail.setBackground(activity.getResources().getDrawable(deviceClass.getMasterStatus()==0?R.drawable.white_circle_border:R.drawable.yellow_circle));
            viewHolder.removeDevice.setOnClickListener(view -> {

                    showAlert(position);

            });

//            viewHolder.groupCustomize.setOnClickListener(view -> {
//
//                    showDialog(position);

////            });
//        viewHolder.lightDetail.setOnClickListener(v -> {
//            Intent intent = new Intent(activity, HelperActivity.class);
//            intent.putExtra(Constants.MAIN_KEY, Constants.EDIT_LIGHT);
//            intent.putExtra(Constants.LIGHT_DETAIL_KEY,deviceClass);
//            activity.startActivity(intent);
//        });
            viewHolder.groupName.setText(deviceClass.getDeviceName());

        return convertView;
    }

    void showAlert(int position) {

        SiteDeviceClass deviceClass=arrayList.get(position);
        boolean status=deviceClass.getStatus();

        selectedPosition=position;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure to remove  "+deviceClass.getDeviceName()+" from "+groupDetailsClass.getGroupSiteName())
                .setTitle("Remove Light");
        builder.setPositiveButton("Remove", (dialog1, id) -> {
            // User clicked OK button
//            acceptRequest(2,position);
            dialog1.dismiss();
            ByteQueue byteQueue=new ByteQueue();
            byteQueue.push(RxMethodType.REMOVE_SITE_GROUP);
            byteQueue.push(0x01);
            byteQueue.pushU4B(deviceClass.getDeviceUID());
            byteQueue.push(0x00);
//            byteQueue.push(groupDetailsClass.getGroupSiteId());
            AdvertiseTask advertiseTask;
            advertiseTask=new AdvertiseTask(this,activity,5*1000);
            advertiseTask.setByteQueue(byteQueue);
//            advertiseTask.setSearchRequestCode(REMOVE_GROUP_RESPONSE);
            advertiseTask.startAdvertising();

//            Toast.makeText(activity, "Will be soon", Toast.LENGTH_SHORT).show();

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    @Override
    public void onScanSuccess(int successCode, ByteQueue byteQueue)
    {
        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();

        Log.w("MethodType", (int) byteQueue.pop() + "");



        ContentValues contentValues = new ContentValues();
        switch (successCode) {
            case REMOVE_GROUP_RESPONSE:
                int status = byteQueue.pop();
                if (status == 0)
                {
                    contentValues.put(COLUMN_GROUP_SITE_ID, 0);
                    Log.w("DashGroup", sqlHelper.updateDeviceNew(arrayList.get(selectedPosition).getDeviceId (), contentValues)+"");
                    arrayList.remove(selectedPosition);
                    notifyDataSetChanged();
                    editGroupFragment.onResume();
                } else {
                    Toast.makeText(activity, "Cannot remove from group.", Toast.LENGTH_SHORT).show();
                }
                break;
            case LIGHT_STATE_COMMAND_RESPONSE:
                byte[] bytes1=byteQueue.pop4B();
                ArrayUtilities.reverse(bytes1);
                String nodeUid=bytesToHex(bytes1);
                int status2=byteQueue.pop();
                Log.w("ScanningBeacon",nodeUid);
//                                String s = "4d0d08ada45f9dde1e99cad9";
                BigInteger bi = new BigInteger(nodeUid, 16);
                Log.w("Scann",bi+","+status2);
                if(status2==0)
                {
                    contentValues.put(COLUMN_DEVICE_STATUS, arrayList.get(selectedPosition).getStatus()?0:1);
                    boolean status1=arrayList.get(selectedPosition).getStatus();
                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                    arrayList.get(selectedPosition).setStatus(!status1);
                    Log.w("DashGroup", sqlHelper.updateDeviceNew (arrayList.get(selectedPosition).getDeviceId (), contentValues) + ","+status1);
                    notifyDataSetChanged();
                }
                else
                {
//                    arrayList.get(selectedPosition).setStatus();
                    notifyDataSetChanged();
                    Toast.makeText(activity, "State command failed.", Toast.LENGTH_SHORT).show();
                }
                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:
                int status3 = byteQueue.pop();
                if(status3==0)
                    Log.w("IndividualLight", sqlHelper.updateDeviceNew(arrayList.get(selectedPosition).getDeviceId (),contentValues)+"");
                else
                    Toast.makeText(activity, "Failed.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onScanFailed(int errorCode) {

        editGroupFragment.onResume();
        if (animatedProgress == null)
            return;
        animatedProgress.hideProgress();
        notifyDataSetChanged();
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(activity);
        dialogBuilder
                .withTitle("Timeout")
                .withEffect(Effectstype.Newspager)
                .withMessage("Timeout,Please check your beacon is in range")
                .withButton1Text("OK")
                .setButton1Click(v -> {
                    dialogBuilder.dismiss();
                })
                .show();
//        Toast.makeText(activity, "Cannot get response from beacon ,make sure your beacon is in range ", Toast.LENGTH_SHORT).show();
        Log.w("StartFailed", errorCode + "");
    }

    @Override
    public void onSuccess(String message) {
//        advertise=false;
//        animatedProgress.showProgress();
        Log.w(TAG,"Advertising start");
    }

    @Override
    public void onFailed(String errorMessage) {
        Toast.makeText(activity, "Cannot advertise.", Toast.LENGTH_SHORT).show();
//        Toast.makeText(activity, "Cannot get response from beacon ,make sure your beacon is in range ", Toast.LENGTH_SHORT).show();
        Log.w(TAG,"onScanFailed "+errorMessage);
    }

    @Override
    public void onStop(String stopMessage, int resultCode) {
//        scannerTask.setRequestCode(resultCode);
//        scannerTask.start();
        if (animatedProgress != null)
            animatedProgress.hideProgress();




        ContentValues contentValues = new ContentValues();
        switch (resultCode) {
            case REMOVE_GROUP_RESPONSE:

                    contentValues.put(COLUMN_GROUP_SITE_ID, 0);
                    Log.w("DashGroup", sqlHelper.updateDeviceNew(arrayList.get(selectedPosition).getDeviceId (), contentValues)+"");
                    arrayList.remove(selectedPosition);
                    notifyDataSetChanged();
                    editGroupFragment.onResume();

                break;
            case LIGHT_STATE_COMMAND_RESPONSE:

                    contentValues.put(COLUMN_DEVICE_STATUS, arrayList.get(selectedPosition).getStatus()?0:1);
                    boolean status1=arrayList.get(selectedPosition).getStatus();
                    Toast.makeText(activity, "status"+status1, Toast.LENGTH_SHORT).show();
                    arrayList.get(selectedPosition).setStatus(!status1);
                    Log.w("DashGroup", sqlHelper.updateDeviceNew(arrayList.get(selectedPosition).getDeviceId (), contentValues) + ","+status1);
                    notifyDataSetChanged();

                break;

            case LIGHT_LEVEL_COMMAND_RESPONSE:

                    Log.w("IndividualLight", sqlHelper.updateDeviceNew(arrayList.get(selectedPosition).getDeviceId (),contentValues)+"");


        }
        Log.w(TAG,"Advertising stop");

    }




    static class ViewHolder {

        @BindView(R.id.remove_from_group)
        Button removeDevice;

        @BindView(R.id.group_customize)
        Button groupCustomize;

        @BindView(R.id.edit_group_name)
        TextView groupName;
        @BindView(R.id.review_1)
        ImageView lightDetail;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void saveGroup()
    {
            if (selectedList.size()<1)
            {
                Toast.makeText(activity, "Group update successfully", Toast.LENGTH_SHORT).show();
                activity.onBackPressed();
                return;
            }
            try {
                int i=0;
                for (String s:selectedList)
                {
                    SiteDeviceClass deviceClass=arrayList.get(Integer.parseInt(s));
                    AdvertiseTask advertiseTask;
                    ByteQueue byteQueue=new ByteQueue();
                    byteQueue.push(RxMethodType.REMOVE_GROUP);   //// remove group method type
                    byteQueue.pushU4B(deviceClass.getDeviceUID());
                    byteQueue.push(groupDetailsClass.getGroupSiteId());
                    advertiseTask=new AdvertiseTask(this,activity,5*1000);
                    advertiseTask.setByteQueue(byteQueue);
                    advertiseTask.startAdvertising();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseConstant.COLUMN_GROUP_SITE_ID,0);
                    sqlHelper.updateDeviceNew(deviceClass.getDeviceId (),contentValues);
                    i++;
                    if (i==selectedList.size())
                    {
                        animatedProgress.showProgress();

                        scannerTask=new ScannerTask(activity,this);
                        scannerTask.setRequestCode(REMOVE_GROUP_RESPONSE);
                        scannerTask.start();

                        Toast.makeText(activity, " Group updated successfully.", Toast.LENGTH_SHORT).show();
                        activity.onBackPressed();
                    }
                }

            }catch (Exception e)
            {
                Log.w("Edit Group",e.getMessage());
            }


    }



}
