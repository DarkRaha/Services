package com.darkraha.services.core.deferred

import com.darkraha.services.core.job.JobResponse
import com.darkraha.services.core.job.JobState


/**
 *
 */
class ChainBlock<PREVRESULT, NEXTPARAM, NEXTRESULT>(
    var mPrevTask: Deferred<*, PREVRESULT>,
    var mNextTask: Deferred<NEXTPARAM, NEXTRESULT>
) {

    private var iniNextTask: ((JobResponse<PREVRESULT>, params: NEXTPARAM?, builder: Any?) -> NEXTPARAM?)? =
        null

    var params: NEXTPARAM? = null
        private set
    var builder: Any? = null
        private set

    private fun initNext(): Deferred<*, NEXTRESULT> {
        iniNextTask?.also { func ->
            mNextTask.job.params = func.invoke(mPrevTask.getResponse(), params, builder)
        }
        return mNextTask
    }

    fun startNext() {
        val state = mPrevTask.job.getState()
        if (state.isFinished()) {
            when {
                state == JobState.ERROR -> mNextTask.setError(mPrevTask.getResponse().getError())
                state == JobState.CANCELED -> mNextTask.setReject(
                    mPrevTask.getResponse().getReason() ?: "Previous task in chain was canceled"
                )
            }

            initNext().async()
        }
    }

    class ChainNextBuilder<PREVRESULT, NEXTPARAM, NEXTRESULT>(
        prev: Deferred<*, PREVRESULT>,
        next: Deferred<NEXTPARAM, NEXTRESULT>
    ) {
        private val chainBlock = ChainBlock(prev, next)

        fun setParam(param: NEXTPARAM): ChainNextBuilder<PREVRESULT, NEXTPARAM, NEXTRESULT> {
            chainBlock.params = param
            return this
        }

        fun setBuilder(builder: Any): ChainNextBuilder<PREVRESULT, NEXTPARAM, NEXTRESULT> {
            chainBlock.builder = builder
            return this
        }

        fun setIniNextParam(func: (response: JobResponse<PREVRESULT>, params: NEXTPARAM?, builder: Any?) -> NEXTPARAM?)
                : ChainNextBuilder<PREVRESULT, NEXTPARAM, NEXTRESULT> {
            chainBlock.iniNextTask = func
            return this
        }

        fun build(): UserDeferred<NEXTRESULT> {
            chainBlock.mPrevTask.chainNext = chainBlock
            chainBlock.startNext()
            return chainBlock.mNextTask
        }
    }
}

