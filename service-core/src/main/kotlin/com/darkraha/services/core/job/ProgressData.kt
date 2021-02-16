/**
 * Interface and class for holding progress of job.
 * @author Rahul Verma
 */

package com.darkraha.services.core.job

/**
 * Notify listeners about current progress of job.
 *
 * @author Rahul Verma
 */
interface JobNotifyProgress {
    fun notifyProgress(dataProgress: ProgressData)
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


