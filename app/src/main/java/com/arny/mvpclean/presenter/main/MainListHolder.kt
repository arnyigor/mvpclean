package com.arny.mvpclean.presenter.main

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.arny.arnylib.adapters.BindableViewHolder
import com.arny.arnylib.files.FileUtils
import com.arny.mvpclean.R
import com.arny.mvpclean.data.models.CleanFolder

class MainListHolder(itemView: View) : BindableViewHolder<CleanFolder>(itemView), View.OnClickListener {

    private var pos: Int = 0
    private var mainActionListener: MainActionListener? = null

    override fun bindView(context: Context, position: Int, item: CleanFolder, actionListener: BindableViewHolder.ActionListener) {
        super.bindView(context, position, item, actionListener)
        this.pos = position
        mainActionListener = actionListener as MainActionListener
        initUI(item)
    }

    private fun initUI(item: CleanFolder) {
        itemView.findViewById<TextView>(R.id.tvFolderName).text = item.title
        itemView.findViewById<TextView>(R.id.tvFolderPath).text = item.path
        val formatFileSize = FileUtils.formatFileSize(item.size, 3)
        itemView.findViewById<TextView>(R.id.tvFolderSize).text = formatFileSize
        itemView.findViewById<ImageView>(R.id.ivRemoveFolder).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivRemoveFolder -> {
                mainActionListener?.onRemove(pos)
            }
        }
    }

    interface MainActionListener : BindableViewHolder.ActionListener {
        fun onRemove(position: Int)
    }
}