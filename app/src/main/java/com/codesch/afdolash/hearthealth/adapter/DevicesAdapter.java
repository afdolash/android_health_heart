package com.codesch.afdolash.hearthealth.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codesch.afdolash.hearthealth.R;
import com.codesch.afdolash.hearthealth.activity.MainActivity;
import com.codesch.afdolash.hearthealth.modal.Devices;

import java.util.List;

/**
 * Created by Afdolash on 8/10/2017.
 */

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder> {
    private Context context;
    private List<Devices> devicesList;

    //An EXTRA to take the device MAC to the next activity
    public static String EXTRA_DEVICE_ADDRESS;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView btName, btAddress;
        public CardView cardDevice;

        public MyViewHolder(View itemView) {
            super(itemView);
            btName = (TextView) itemView.findViewById(R.id.tv_btDeviceName);
            btAddress = (TextView) itemView.findViewById(R.id.tv_btDeviceAddress);
            cardDevice = (CardView) itemView.findViewById(R.id.cardDevice);
        }
    }

    public DevicesAdapter(Context context, List<Devices> devicesList) {
        this.devicesList = devicesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_device, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DevicesAdapter.MyViewHolder holder, int position) {
        final Devices devices = devicesList.get(position);

        holder.btName.setText(devices.getName());
        holder.btAddress.setText(devices.getAddress());
        holder.cardDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Connecting to "+ devices.getAddress() , Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, devices.getAddress());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }
}
