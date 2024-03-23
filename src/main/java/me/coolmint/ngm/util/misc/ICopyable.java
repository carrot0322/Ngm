package me.coolmint.ngm.util.misc;

public interface ICopyable<T extends ICopyable<T>> {
    T set(T value);

    T copy();
}