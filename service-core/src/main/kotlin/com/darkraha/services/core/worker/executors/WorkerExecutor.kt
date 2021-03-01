package com.darkraha.services.core.worker.executors

/**
 * @author Rahul Verma
 */
interface WorkerExecutor<T> {
    /**
     * Executes block immediately, if current thread. Otherwise adds block
     * to the thread queue.
     */
    fun execute(block: () -> Unit): T?

    /**
     * Adds block to the thread queue.
     */
    fun post(block: () -> Unit): T

    /**
     * Adds block to the thread queue after specified time.
     */
    fun postDelayed(timeDelay: Long, block: () -> Unit): T
}