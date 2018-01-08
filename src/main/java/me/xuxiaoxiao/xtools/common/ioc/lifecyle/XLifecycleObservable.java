package me.xuxiaoxiao.xtools.common.ioc.lifecyle;

public interface XLifecycleObservable {
    void registerObserver(XLifecycleObserver observer);

    void unregisterObserver(XLifecycleObserver observer);

    void lifeInitial();

    void lifeDestroy();

    void bindLifecycle(XLifecycleObservable observable);
}
