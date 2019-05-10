package net.minecraft.resources.event;

@FunctionalInterface
public interface Listener<T extends Event> {

	void handle(T event);

}
