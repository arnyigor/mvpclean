package com.arny.mvpclean.presenter.main

import android.util.Log
import com.arny.mvpclean.data.utils.DateTimeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    fun aa_loadList() {
        mPresenter?.loadList()
        verify(mMockView)?.clearList();
    }

    @Test
    fun ab_loadList() {
        val dateTime1 = "16:46"
        val dateTime2 = "16:50"
        val timeLong1 = DateTimeUtils.convertTimeStringToLong(dateTime1, "HH:mm")
        val timeLong2 = DateTimeUtils.convertTimeStringToLong(dateTime2, "HH:mm")
        Log.i(MainPresenterTest::class.java.simpleName, "ab_loadList1: $timeLong1");
        Log.i(MainPresenterTest::class.java.simpleName, "ab_loadList2: $timeLong2")
        val diff = DateTimeUtils.getTimeDiff(timeLong1, timeLong2)
        assertThat(diff).isGreaterThan(0)
        assertThat(diff).isEqualTo(240000)
    }

}