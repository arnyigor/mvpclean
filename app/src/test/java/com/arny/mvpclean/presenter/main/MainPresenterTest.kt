package com.arny.mvpclean.presenter.main

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(MockitoJUnitRunner::class)
class MainPresenterTest {
    @Mock
    private var mMockView: MainContract.View? = null
    private var mPresenter: MainPresenter? = null
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mPresenter = MainPresenter()
    }

    @Test
    fun loadList() {
        mPresenter?.loadList()
        verify(mMockView)?.clearList();
    }

}