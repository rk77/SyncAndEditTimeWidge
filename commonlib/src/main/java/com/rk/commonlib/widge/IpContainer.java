package com.rk.commonlib.widge;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.rk.commonlib.R;

import java.util.ArrayList;

public class IpContainer extends LinearLayout {

    private static final String TAG = IpContainer.class.getSimpleName();

    private LinearLayout mContainer;
    private ArrayList<LinearLayout> mIpEditWidgeList = new ArrayList<>();
    private ArrayList<String> mIpList = new ArrayList<>();
    private ArrayList<String> mPorts = new ArrayList<>();

    private LinearLayout mFirstIpAndPortWdige;

    private Context mContext;

    public IpContainer(Context context) {
        this(context, null);
    }

    public IpContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IpContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IpContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        Log.i(TAG, "init");
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = (LinearLayout) inflater.inflate(R.layout.ip_port_item_add_layout, this, true);
        mContainer = view.findViewById(R.id.container);
        mContainer.setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));
        mContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        mFirstIpAndPortWdige = mContainer.findViewById(R.id.first_ip_and_port);
        mIpEditWidgeList.add(mFirstIpAndPortWdige);
        ImageButton addBtn = mFirstIpAndPortWdige.findViewById(R.id.add);
        ImageButton delBtn = mFirstIpAndPortWdige.findViewById(R.id.remove);

        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        delBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(mFirstIpAndPortWdige);
            }
        });
    }

    private void addItem() {
        Log.i(TAG, "addItem, whole: " + mIpEditWidgeList.size());
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout ipAndPortWidge = (LinearLayout) inflater.inflate(R.layout.ip_port_item_layout, null, false);
        ImageButton addBtn = ipAndPortWidge.findViewById(R.id.add);
        ImageButton delBtn = ipAndPortWidge.findViewById(R.id.remove);

        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        delBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(ipAndPortWidge);
            }
        });
        mIpEditWidgeList.add(ipAndPortWidge);
        mContainer.addView(ipAndPortWidge);
        requestLayout();

    }

    private void removeItem(LinearLayout ipAndPortWidge) {
        Log.i(TAG, "removeItem, whole size: " + mIpEditWidgeList.size());
        if (mIpEditWidgeList.size() <= 1) {
            return;
        }

        if (mIpEditWidgeList.contains(ipAndPortWidge)) {
            mIpEditWidgeList.remove(ipAndPortWidge);
            mContainer.removeView(ipAndPortWidge);
            requestLayout();
        }
    }

    public ArrayList<String> getAllIp() {
        ArrayList<String> ips = new ArrayList<>();
        if (mIpEditWidgeList.size() > 0) {
            for (int i = 0; i < mIpEditWidgeList.size(); i++) {
                LinearLayout ipAndPortWidge = mIpEditWidgeList.get(i);
                IpEditWidge ipEditWidge = ipAndPortWidge.findViewById(R.id.ip);
                ips.add(ipEditWidge.getIp());
            }
            return ips;
        }
        return null;
    }

    public ArrayList<String> getAllPort() {
        ArrayList<String> ports = new ArrayList<>();
        if (mIpEditWidgeList.size() > 0) {
            for (int i = 0; i < mIpEditWidgeList.size(); i++) {
                LinearLayout ipAndPortWidge = mIpEditWidgeList.get(i);
                EditText portWidge = ipAndPortWidge.findViewById(R.id.port);
                String s = portWidge.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    s = "0";
                }
                ports.add(s);
            }
            return ports;
        }
        return null;
    }

    public ArrayList<String> getAllIpAndPort() {
        ArrayList<String> ipAndPorts = new ArrayList<>();
        if (mIpEditWidgeList.size() > 0) {
            for (int i = 0; i < mIpEditWidgeList.size(); i++) {
                LinearLayout ipAndPortWidge = mIpEditWidgeList.get(i);
                IpEditWidge ipWidge = ipAndPortWidge.findViewById(R.id.ip);
                EditText portWidge = ipAndPortWidge.findViewById(R.id.port);
                String ip = ipWidge.getIp();
                String port = portWidge.getText().toString();
                if (TextUtils.isEmpty(port)) {
                    port = "0";
                }
                if (TextUtils.isEmpty(ip)) {
                    ip = "0.0.0.0";
                }
                StringBuilder sb = new StringBuilder();
                sb.append(ip).append(":").append(port);
                ipAndPorts.add(sb.toString());
            }
            return ipAndPorts;
        }
        return null;
    }

    public void setAllIpAndPort(ArrayList<String> ips) {
        if (ips == null || ips.size() <= 0) {
            return;
        }
        int count = mIpEditWidgeList.size();
        if (count > ips.size()) {
            for (int i = 0; i < count - ips.size(); i++) {
                LinearLayout item = mIpEditWidgeList.get(mIpEditWidgeList.size() - 1);
                removeItem(item);
            }
        } else if (count < ips.size()){
            for (int i = 0; i < ips.size() - count; i++) {
                addItem();
            }

        }
        for (int i = 0; i < ips.size(); i++) {
            String ip_port_item = ips.get(i);
            if (ip_port_item == null || ip_port_item.split(":") == null || ip_port_item.split(":").length == 2) {
                continue;
            }

            String ip = ip_port_item.split(":")[0];
            String port = ip_port_item.split(":")[1];

            LinearLayout ipAndPortWidge = mIpEditWidgeList.get(i);
            IpEditWidge ipWidge = ipAndPortWidge.findViewById(R.id.ip);
            EditText portWidge = ipAndPortWidge.findViewById(R.id.port);

            ipWidge.setIp(ip);
            portWidge.setText(port);
        }
    }
}
