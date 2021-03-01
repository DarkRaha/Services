package com.darkraha.services.core.job

/**
 * @author Rahul Verma
 */
enum class JobState {
    PREPARE,
    PENDING,
    PROGRESS,
    SUCCESS,
    ERROR,
    CANCELED,
    FINISHED
    ;

    fun isIgnoreAdd(forState: JobState): Boolean {
        when (forState) {
            PREPARE -> return false
            PENDING -> return this != PREPARE
            PROGRESS -> return isFinished()
            SUCCESS, ERROR, CANCELED, FINISHED -> return !(this == PREPARE || this == PENDING)
        }
    }

    fun isAllowExecute(forState: JobState): Boolean {
        return when (forState) {
            PROGRESS -> this == PENDING
            FINISHED -> this.isFinished()
            else -> forState == this
        }

    }

    fun isFinished() = this > PROGRESS //!(this == PREPARE || this == PENDING || this == PROGRESS)
}
