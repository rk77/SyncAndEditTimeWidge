package com.rk.commonlib.widge.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rk.commonlib.R;

import java.util.List;

public class SimpleRecyclerAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<String> mTitleList;
    private OnItemClickListener mListener;

    public SimpleRecyclerAdapter(Context context, List<String> titleList) {
        mContext = context;
        mTitleList = titleList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.icon_title_vertical_item_layout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (mTitleList == null || mTitleList.size() <= 0) {
            return;
        }
        ((SimpleViewHolder) holder).mImageView.setImageResource(R.drawable.work_sheet_icon);
        ((SimpleViewHolder) holder).mTextView.setText(mTitleList.get(position));
        ((SimpleViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mTitleList == null || mTitleList.size() <= 0) {
            return 0;
        }
        return mTitleList.size();
    }

    public void setData(List<String> titleList) {
        mTitleList = titleList;
    }



    private class SimpleViewHolder extends RecyclerView.ViewHolder{

        private ImageView mImageView;
        private TextView mTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_icon);
            mTextView = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
