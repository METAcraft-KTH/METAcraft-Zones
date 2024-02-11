package se.datasektionen.mc.zones.util;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class LockHelper {

	public static <T> T getThroughLock(Lock lock, Supplier<T> modify) {
		lock.lock();
		var result = modify.get();
		lock.unlock();
		return result;
	}

}
