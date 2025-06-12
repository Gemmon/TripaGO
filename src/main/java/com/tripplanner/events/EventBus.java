package com.tripplanner.events;

import org.apache.commons.lang3.reflect.MethodUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {
    private static EventBus instance;
    private final List<Object> subscribers = new CopyOnWriteArrayList<>();
    private final ExecutorService eventExecutor = Executors.newFixedThreadPool(7);

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void register(Object subscriber) {
        subscribers.add(subscriber);
    }

    public void unregister(Object subscriber) {
        subscribers.remove(subscriber);
    }

    public void post(Object event) {
        eventExecutor.submit(() -> {
            for (Object subscriber : subscribers) {
                try {
                    // Reflection API
                    Method[] methods = MethodUtils.getMethodsWithAnnotation(subscriber.getClass(), Subscribe.class);
                    for (Method method : methods) {
                        if (method.getParameterTypes().length == 1 &&
                                method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                            method.setAccessible(true);
                            method.invoke(subscriber, event);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}