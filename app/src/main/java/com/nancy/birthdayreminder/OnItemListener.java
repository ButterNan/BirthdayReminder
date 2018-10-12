package com.nancy.birthdayreminder;

import android.view.View;

public interface OnItemListener {

        void onRowClicked(View view, int position);

        void onCheckBoxChecked(View view, int position);

        void onCheckBoxUnChecked(View view, int position);

        //void onSyncButtonClicked(View itemView, int adapterPosition);
}
