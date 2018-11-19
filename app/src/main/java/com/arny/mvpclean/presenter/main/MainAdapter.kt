package com.arny.mvpclean.presenter.main

import com.arny.mvpclean.R
import com.arny.mvpclean.data.adapters.SimpleAbstractAdapter
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.utils.FileUtils
import kotlinx.android.synthetic.main.files_list_item.view.*

class MainAdapter(private val folderClickListener: FolderClickListener) : SimpleAbstractAdapter<CleanFolder>() {
    override fun getLayout(): Int {
        return R.layout.files_list_item
    }

    interface FolderClickListener : OnViewHolderListener<CleanFolder> {
        fun onRemove(position: Int, item: CleanFolder)
    }

    override fun bindView(item: CleanFolder, viewHolder: VH) {
        viewHolder.itemView.apply {
            tvFolderName.text = item.title
            tvFolderPath.text = item.path
            val formatFileSize = FileUtils.formatFileSize(item.size, 3)
            tvFolderSize.text = formatFileSize
            ivRemoveFolder.setOnClickListener {
                folderClickListener.onRemove(viewHolder.adapterPosition, item)
            }
        }
    }

    override fun getDiffCallback(): DiffCallback<CleanFolder>? {
        return object : DiffCallback<CleanFolder>() {
            override fun areItemsTheSame(oldItem: CleanFolder, newItem: CleanFolder): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CleanFolder, newItem: CleanFolder): Boolean {
                return oldItem.path == newItem.path
            }
        }
    }
}