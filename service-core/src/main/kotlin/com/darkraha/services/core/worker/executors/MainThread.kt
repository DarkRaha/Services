package com.darkraha.services.core.worker.executors

import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
/**
 * Main thread proxy object. If a native main thread does not exist on the current platform,
 * then the thread pool with a a single thread is used.
 * @author Rahul Verma
 */
object MainThread : WorkerExecutor<Job>{

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


    //---------------------------------------------------------
    //  block: suspend CoroutineScope.() -> Unit

    override fun execute(block: () -> Unit) = if (isMainPresent) scope.launch((dispatcher as MainCoroutineDispatcher).immediate) {
            block()
        }
        else scope.launch(context = dispatcher, start = CoroutineStart.UNDISPATCHED) {
            block()
        }


    override fun post(block: () -> Unit) = scope.launch(context = dispatcher) {
        block()
    }

    override fun postDelayed(timeDelay: Long, block: () -> Unit) = scope.launch {
        delay(timeDelay)
        block()
    }

}