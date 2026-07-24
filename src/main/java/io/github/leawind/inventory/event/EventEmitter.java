package io.github.leawind.inventory.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Minimal event emitter vendored from Leawind's inventory-java (MIT), reduced to the subset used by
 * Third Person Plus (keyed one-time listeners + emit).
 *
 * <p>Keyed one-time listeners with the same key replace each other. Keyless listeners accumulate.
 * All one-time listeners are removed after the next {@link #emit()}.
 *
 * @param <E> the event type
 */
public class EventEmitter<E> {
  private final List<Consumer<E>> persistent = new ArrayList<>();
  private final List<Consumer<E>> anonymousOnce = new ArrayList<>();
  private final Map<Object, Consumer<E>> keyedOnce = new LinkedHashMap<>();

  /** Register a persistent listener. */
  public EventEmitter<E> on(Consumer<E> listener) {
    persistent.add(listener);
    return this;
  }

  /** Register a keyless one-time listener, removed after the next emit. */
  public EventEmitter<E> once(Consumer<E> listener) {
    anonymousOnce.add(listener);
    return this;
  }

  /** Register a keyed one-time listener; a later call with the same key replaces the previous one. */
  public EventEmitter<E> once(Object key, Consumer<E> listener) {
    keyedOnce.put(key, listener);
    return this;
  }

  public boolean hasKey(Object key) {
    return keyedOnce.containsKey(key);
  }

  public EventEmitter<E> clear() {
    persistent.clear();
    anonymousOnce.clear();
    keyedOnce.clear();
    return this;
  }

  /** Emit with a {@code null} event (convenience for {@code EventEmitter<Void>}). */
  public void emit() {
    emit(null);
  }

  public void emit(E event) {
    for (var listener : new ArrayList<>(persistent)) {
      listener.accept(event);
    }
    var anon = new ArrayList<>(anonymousOnce);
    anonymousOnce.clear();
    for (var listener : anon) {
      listener.accept(event);
    }
    var keyed = new ArrayList<>(keyedOnce.values());
    keyedOnce.clear();
    for (var listener : keyed) {
      listener.accept(event);
    }
  }
}
