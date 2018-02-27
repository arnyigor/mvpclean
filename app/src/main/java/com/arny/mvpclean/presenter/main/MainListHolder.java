package com.arny.mvpclean.presenter.main;

import android.content.Context;
import android.view.View;
import com.arny.arnylib.adapters.BindableViewHolder;
import com.arny.mvpclean.data.models.CleanFolder;
public class MainListHolder extends BindableViewHolder<CleanFolder> implements View.OnClickListener {

    private int position;
    private MainActionListener mainActionListener;

    public MainListHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindView(Context context, int position, CleanFolder item, ActionListener actionListener) {
        super.bindView(context, position, item, actionListener);
        this.position = position;
	    mainActionListener = (MainActionListener) actionListener;
	    initUI(item);
    }


    private void initUI(CleanFolder item) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    public interface MainActionListener extends ActionListener {
    }
}