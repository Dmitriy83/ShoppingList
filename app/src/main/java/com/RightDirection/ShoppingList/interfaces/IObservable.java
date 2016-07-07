package com.RightDirection.ShoppingList.interfaces;

// Реализация шаблона Наблюдатель
public interface IObservable {
    void addObserver(IObserver observer);
    void removeObserver(IObserver observer);
    void notifyObservers();
}
