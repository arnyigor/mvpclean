package com.arny.mvpclean.data.repo;

import android.util.Log;
import com.arny.mvpclean.CleanApp;
import com.arny.mvpclean.R;
import com.arny.mvpclean.presenter.MainContract;
import io.reactivex.Observable;

import java.sql.Time;
public class MainRepository implements MainContract.Repository {

	private String longTimeLoadingOperation() {
		Log.d(MainRepository.class.getSimpleName(), "loadString: ");
		/* Здесь обращаемся к БД или сети.
		 * DBHelper'ами и прочими не относяшимеся к теме объектами.
		 */
		String string = CleanApp.getContext().getResources().getString(R.string.app_name);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "Вернули результат:" + string;
	}

	@Override
	public Observable<String> loadString() {
		return Observable.fromCallable(this::longTimeLoadingOperation);
	}
}