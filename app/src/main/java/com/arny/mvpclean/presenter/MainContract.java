package com.arny.mvpclean.presenter;
import io.reactivex.Observable;
public interface MainContract {
	interface View extends MvpView {
		void showText(String text);
		void showLoading(String text);
		void hideLoading();
	}

	interface Presenter extends MvpPresenter<View> {
		void onButtonWasClicked();
	}

	interface Repository {
		Observable<String> loadString();
	}
}
