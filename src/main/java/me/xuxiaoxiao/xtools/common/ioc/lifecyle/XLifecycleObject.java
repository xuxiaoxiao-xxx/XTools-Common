package me.xuxiaoxiao.xtools.common.ioc.lifecyle;

import java.util.LinkedList;
import java.util.List;

public class XLifecycleObject implements XLifecycleObservable {
    private final List<XLifecycleObserver> observers = new LinkedList<>();

    @Override
    public void registerObserver(XLifecycleObserver observer) {
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    @Override
    public void unregisterObserver(XLifecycleObserver observer) {
        synchronized (observers) {
            if (observers.contains(observer)) {
                observers.remove(observer);
            }
        }
    }

    @Override
    public void lifeInitial() {
        synchronized (observers) {
            for (XLifecycleObserver observer : observers) {
                observer.onInitial();
            }
        }
    }

    @Override
    public void lifeDestroy() {
        synchronized (observers) {
            for (XLifecycleObserver observer : observers) {
                observer.onDestroy();
            }
        }
    }

    @Override
    public void bindLifecycle(XLifecycleObservable observable) {
        observable.registerObserver(new BindObserver(this));
    }

    public static class BindObserver implements XLifecycleObserver {
        public final XLifecycleObservable binder;

        public BindObserver(XLifecycleObservable binder) {
            this.binder = binder;
        }


        @Override
        public void onInitial() {
            binder.lifeInitial();
        }

        @Override
        public void onDestroy() {
            binder.lifeDestroy();
        }
    }
}
