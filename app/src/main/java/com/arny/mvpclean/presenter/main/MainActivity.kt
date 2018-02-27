package com.arny.mvpclean.presenter.main

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import com.arny.arnylib.adapters.SimpleBindableAdapter
import com.arny.arnylib.utils.BasePermissions
import com.arny.mvpclean.R
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.presenter.base.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View,FolderChooserDialog.FolderCallback {
    override fun clearList() {
        adapter?.clear()
    }

    override fun updateInfo(text: String) {
        tvCleanFilesInfo.text = text
    }

    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        mPresenter.addFolder(folder)
    }

    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {
        onResume()
    }

    override fun showAddDialog() {
        FolderChooserDialog.Builder(this)
                .chooseButton(R.string.choose_folder)  // changes label of the choose button
                .initialPath(Environment.getExternalStorageDirectory().getPath())
                .tag("folder_choose")
                .goUpLabel("Вверх")
                .show(this)
    }

    override fun updateList() {
        adapter?.notifyDataSetChanged()
    }

    override fun showTotalSize(size: String) {
        tvCleanFilesInfo.text = size
    }

    private var adapter: SimpleBindableAdapter<CleanFolder, MainListHolder>? = null
    override var mPresenter: MainPresenter = MainPresenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            title = "Очистка директорий"
        }
        setUpList()
    }

    override fun onResume() {
        super.onResume()
        mPresenter.loadList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_file -> {
                if (!BasePermissions.isStoragePermissonGranted(this)) {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_SMS),
                            101)
                    return false
                }
                showAddDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permissionGranted = BasePermissions.permissionGranted(grantResults)
        when {
            requestCode == 101 && permissionGranted -> showAddDialog()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun setUpList() {
        rvFiles.layoutManager = LinearLayoutManager(this)
        adapter = SimpleBindableAdapter(this, R.layout.files_list_item, MainListHolder::class.java)
        rvFiles.adapter = adapter
    }


    override fun showList(list: MutableList<CleanFolder>) {
        adapter?.addAll(list)
    }

}
