package com.darkraha.services.core.worker.executors

import java.util.concurrent.*

class TPoolWorkerExecutor(val exe: ExecutorService) : WorkerExecutor<Future<*>> {
    constructor(maxCore: Int) : this(
        ThreadPoolExecutor(
            0, maxCore, 2L, TimeUnit.MINUTES,
            LinkedBlockingDeque()
        )
    )

    var prefix: String? = null
        protected set

    override fun execute(block: () -> Unit): Future<*>? {
        val p = prefix
        if (p != null) {
            val name = Thread.currentThread().name
            if (name.startsWith(p)) {
                block.invoke()
                return null
            }
        } else {
            exe.submit {
                val name = Thread.currentThread().name
                prefix = name.substring(0, name.indexOf('-', 5) + 1)
            }
        }
        return exe.submit(block)
    }

    override fun post(block: () -> Unit): Future<*> {
        return exe.submit(block)
    }

    override fun postDelayed(timeDelay: Long, block: () -> Unit): Future<*> {
        TODO("Not yet implemented")
    }
}