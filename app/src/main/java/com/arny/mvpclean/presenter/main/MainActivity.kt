package com.arny.mvpclean.presenter.main

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import com.arny.arnylib.adapters.SimpleBindableAdapter
import com.arny.arnylib.interfaces.ConfirmDialogListener
import com.arny.arnylib.presenter.base.BaseMvpActivity
import com.arny.arnylib.utils.BasePermissions
import com.arny.arnylib.utils.ToastMaker
import com.arny.arnylib.utils.confirmDialog
import com.arny.mvpclean.R
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.presenter.schedule.ScheduleCleanDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View, FolderChooserDialog.FolderCallback, View.OnClickListener {

    override fun toastSuccess(message: String) {
        ToastMaker.toastSuccess(this,message)
    }

    override fun updateBtn(enable: Boolean) {
        btnClean.isEnabled = enable
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnClean -> {
                confirmDialog(this, "Подтвердите удаление", "Удалить файлы с диска?", dialogListener = object : ConfirmDialogListener {
                    override fun onConfirm() {
                        mPresenter.cleanFolders()
                    }

                    override fun onCancel() {
                    }
                })
            }
            R.id.ivEditSchedule->{
                val dialog = ScheduleCleanDialog(this, object : ScheduleCleanDialog.OnSheduleListener {
                    override fun onSheduleSet(scheduleData: ScheduleData?) {
                        tvSchedule.text = "Расписание:Время-${scheduleData?.time} Периодически:${scheduleData?.isRepeat}"
                        mPresenter.setSchedule(scheduleData)
                    }
                } )
                dialog.show()
            }
            R.id.ivRemoveSchedule->{
                mPresenter.setSchedule(ScheduleData())
            }
        }
    }

    override fun showMessage(message: String) {
        tvCleanFilesInfo.text = message
    }

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
    override val mPresenter = MainPresenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Очистка директорий"
        setUpList()
        mPresenter.loadList()
        btnClean.setOnClickListener(this)
        ivEditSchedule.setOnClickListener(this)
        ivRemoveSchedule.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        adapter?.items?.let { mPresenter.updateFoldersSize(it) }
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
        rvFiles.layoutManager = LinearLayoutManager(this@MainActivity)
        adapter = SimpleBindableAdapter(this, R.layout.files_list_item, MainListHolder::class.java)
        rvFiles.adapter = adapter
        adapter?.setActionListener(object : MainListHolder.MainActionListener {
            override fun onItemClick(position: Int, Item: Any?) {
            }

            override fun onRemove(position: Int) {
                confirmDialog(this@MainActivity, "Подтвердите удаление", content = "Удалить из списка " + adapter?.getItem(position)?.title + "?", dialogListener = object : ConfirmDialogListener {
                    override fun onConfirm() {
                        adapter?.items?.let { mPresenter.removeFolderItem(position, it) }
                    }

                    override fun onCancel() {
                    }
                })
            }
        })
    }

    override fun showList(list: MutableList<CleanFolder>) {
        adapter?.addAll(list)
    }

}
