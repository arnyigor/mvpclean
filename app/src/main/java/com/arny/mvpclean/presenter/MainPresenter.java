package com.arny.mvpclean.presenter;

import android.util.Log;
import com.arny.mvpclean.CleanApp;
import com.arny.mvpclean.R;
import com.arny.mvpclean.UtilsKt;
import com.arny.mvpclean.data.repo.MainRepository;
import io.reactivex.disposables.CompositeDisposable;
public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
	//Компоненты MVP приложения
	private MainContract.Repository mRepository;
	private final CompositeDisposable disposable = new CompositeDisposable();
	//Сообщение
	private String message = CleanApp.getContext().getString(R.string.app_name);

	public MainPresenter(MainContract.View mView) {
		Log.d(MainPresenter.class.getSimpleName(), "MainPresenter: constructor mView:" + mView);
		attachView(mView);
		this.mRepository = new MainRepository();
	}

	@Override
	public void onButtonWasClicked() {
		Log.d(MainPresenter.class.getSimpleName(), "onButtonWasClicked: ");
		getView().showLoading("Загрузка контента");
		disposable.add(UtilsKt.mainThreadObservable(mRepository.loadString())
				.doOnSubscribe(disposable -> getView().showLoading("Загрузка контента"))
				.subscribe(s -> {
					message = s;
					getView().showText(message);
				}, throwable -> {
					throwable.printStackTrace();
					getView().showText("Ошибка загрузки:" + throwable.getMessage());
				}));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		/*Если бы мы работали например с RxJava, в этом классе стоило бы отписываться от подписок
		Кроме того, при работе с другими методами асинхронного андроида,здесь мы боремся с утечкой контекста*/
		disposable.clear();
		Log.d(MainPresenter.class.getSimpleName(), "onDestroy: ");
	}

	@Override
	public void viewIsReady() {
		getView().showText(message);
	}

	@Override
	public void detachView() {
		Log.d(MainPresenter.class.getSimpleName(), "detachView: ");
	}
}
