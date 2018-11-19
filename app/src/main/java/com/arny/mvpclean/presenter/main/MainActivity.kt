package com.arny.mvpclean.presenter.main

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.arny.mvpclean.R
import com.arny.mvpclean.data.dialogs.ConfirmDialogListener
import com.arny.mvpclean.data.dialogs.confirmDialog
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.data.utils.ToastMaker
import com.arny.mvpclean.presenter.base.BaseMvpActivity
import com.arny.mvpclean.presenter.schedule.ScheduleCleanDialog
import com.obsez.android.lib.filechooser.ChooserDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View, View.OnClickListener {
    private var adapter: MainAdapter? = null

    override fun initPresenter(): MainPresenter {
        return MainPresenter()
    }

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

    override fun setSchelude(msg: String) {
        tvSchedule.text = msg
    }

    override fun showLastCleanTime(time: String) {
        tv_last_clean_time.text = time
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

    override fun showAddDialog() {
        ChooserDialog(this)
                .withFilter(true, false)
                .withDateFormat("HH:mm")
                .withNavigateUpTo { true }
                .withNavigateTo { true }
                .withStartFile(Environment.getExternalStorageDirectory().path)
                .withChosenListener { _, pathFile ->
                    mPresenter.addFolder(pathFile)
                }
                .build()
                .show()

    }

    override fun updateList() {
        adapter?.notifyDataSetChanged()
    }

    override fun showTotalSize(size: String) {
        tvCleanFilesInfo.text = size
    }

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
        adapter?.getItems()?.let { mPresenter.updateFoldersSize(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_file -> {
                RxPermissions(this)
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe { granted ->
                            if (granted) {
                                showAddDialog()
                            } else {
                                Log.i(MainActivity::class.java.simpleName, "onOptionsItemSelected: permission denied");
                            }
                        }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpList() {
        rvFiles.layoutManager = LinearLayoutManager(this@MainActivity)
        adapter = MainAdapter(object : MainAdapter.FolderClickListener {
            override fun onRemove(position: Int, item: CleanFolder) {
                confirmDialog(this@MainActivity, "Подтвердите удаление", content = "Удалить из списка " + adapter?.getItem(position)?.title + "?", dialogListener = object : ConfirmDialogListener {
                    override fun onConfirm() {
                        val items = adapter?.getItems()
                        items?.let { mPresenter.removeFolderItem(position, it) }
                    }

                    override fun onCancel() {
                    }
                })
            }

            override fun onItemClick(position: Int, item: CleanFolder) {
            }
        })
        rvFiles.adapter = adapter
    }

    override fun showList(list: MutableList<CleanFolder>) {
        adapter?.addAll(list)
    }

}
