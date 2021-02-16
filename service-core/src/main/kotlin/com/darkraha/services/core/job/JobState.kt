package com.darkraha.services.core.job

/**
 * @author Rahul Verma
 */
enum class JobState {
    PREPARE,
    PENDING,
    SUCCESS,
    ERROR,
    CANCELED,
    FINISHED
    ;

    fun isIgnoreAdd(forState: JobState): Boolean {
        when (forState) {
            PREPARE -> return false
            PENDING -> return this != PREPARE
            SUCCESS, ERROR, CANCELED, FINISHED -> return !(this == PREPARE || this == PENDING)
        }

    }


    fun isFinished() = !(this == PREPARE || this == PENDING)
}
