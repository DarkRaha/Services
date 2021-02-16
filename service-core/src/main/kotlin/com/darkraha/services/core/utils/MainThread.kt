package com.darkraha.services.core.utils

import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * Marker that code should be executed in main thread
 */
interface OnMainThread

/**
 * Main thread proxy object. If a native main thread does not exist on the current platform,
 * then the thread pool with a a single thread is used.
 * @author Rahul Verma
 */
object MainThread {

    val MAIN_THREAD_NAME = "Main"

    var isMainPresent = true
        private set

    var scope: CoroutineScope = GlobalScope

    var dispatcher = try {
        Dispatchers.Main.apply { isDispatchNeeded(this) }
    } catch (e: IllegalStateException) {
        isMainPresent = false
        Executors.newFixedThreadPool(1, object : ThreadFactory {
            override fun newThread(r: Runnable?): Thread? {
                return Thread(
                    r,
                    MAIN_THREAD_NAME
                )
            }
        }).asCoroutineDispatcher()
    }

    /**
     * Executes block immediately, if current thread is main/ui thread. Otherwise posts block to
     * main thread.
     */
    fun execute(block: suspend CoroutineScope.() -> Unit) =
        if (isMainPresent) scope.launch((dispatcher as MainCoroutineDispatcher).immediate) {
            block()
        }
        else scope.launch(context = dispatcher, start = CoroutineStart.UNDISPATCHED) {
            block()
        }

    fun post(block: suspend CoroutineScope.() -> Unit) = scope.launch(context = dispatcher) {
        block()
    }

    fun postDelayed(timeDelay: Long, block: suspend CoroutineScope.() -> Unit) = scope.launch {
        delay(timeDelay)
        block()
    }


}