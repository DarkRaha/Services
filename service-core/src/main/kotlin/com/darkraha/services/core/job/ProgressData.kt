/**
 * Interface and class for holding progress of job.
 * @author Rahul Verma
 */

package com.darkraha.services.core.job

import com.darkraha.services.core.utils.MainThread
import com.darkraha.services.core.utils.OnMainThread
import java.lang.ref.WeakReference
import java.util.function.BiConsumer

/**
 * Notify listeners about current progress of job.
 *
 * @author Rahul Verma
 */
interface JobNotifyProgress {
    fun notifyProgress(dataProgress: ProgressData)
}

open class WeakBiConsumer<T, U>(owner: Any, consumer: BiConsumer<T, U>) : BiConsumer<T, U> {

    val ref = WeakReference(owner)
    val refConsumer = WeakReference(consumer)

    override fun accept(t: T, u: U) {
        ref.get()?.apply { refConsumer.get()?.accept(t, u) }
    }
}


class BiConsumerOnMainThread<T, U>(val consumer: BiConsumer<T, U>) : BiConsumer<T, U>, OnMainThread {
    override fun accept(t: T, u: U) {
        MainThread.execute { consumer.accept(t, u) }
    }
}

class WeakBiConsumerOnMainThread<T, U>(objWeak: Any, consumer: BiConsumer<T, U>) :
    WeakBiConsumer<T, U>(objWeak, consumer), OnMainThread {

    override fun accept(t: T, u: U) {
        if (ref.get() != null && refConsumer.get() != null) {
            MainThread.execute { super.accept(t, u) }
        }
    }
}


interface ProgressData {
    val current: Long
    val total: Long
    val action: String
    val currentData: Any?
    val taskState: Any?
    val timePassed: Long
}

class MutableProgressData : ProgressData {
    var mCurrent: Long = 0
    var mTotal: Long = 0
    var mAction: String = "undefined"
    var mCurrentData: Any? = null
    var mState: Any? = null
    var mTimePassed: Long = 0

    override val current: Long
        get() = mCurrent

    override val total: Long
        get() = mTotal

    override val action: String
        get() = mAction

    override val currentData: Any?
        get() = mCurrentData

    override val timePassed: Long
        get() = mTimePassed

    override val taskState: Any?
        get() = mState
}


