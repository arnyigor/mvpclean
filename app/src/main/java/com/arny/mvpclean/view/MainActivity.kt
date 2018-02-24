package com.arny.mvpclean.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.arny.mvpclean.R
import com.arny.mvpclean.presenter.MainContract
import com.arny.mvpclean.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainContract.View, View.OnClickListener {
    private lateinit var mPresenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = MainPresenter(this)
        tvHello.setOnClickListener(this)
    }

    override fun showText(text: String?) {
        tvHello.text = text
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvHello -> mPresenter.onButtonWasClicked()
        }
    }

    override fun showLoading(text: String?) {
       tvHello.text = text
    }

    override fun hideLoading() {
        tvHello.text = ""
    }


    //Вызываем у Presenter метод onDestroy, чтобы избежать утечек контекста и прочих неприятностей.
    override fun onDestroy() {
        super.onDestroy()
        println("MainActivity onDestroy")
        mPresenter.onDestroy()
    }
}
