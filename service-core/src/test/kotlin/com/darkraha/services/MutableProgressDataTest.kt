package com.darkraha.services

import com.darkraha.services.core.job.MutableProgressData
import com.darkraha.services.core.job.ProgressData
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MutableProgressDataTest {

    @Test
    fun test() {
        val action1 = "undefined"
        val action2 = "download"
        val data1 = "Hello world!"
        val data2 = "Test"

        val pd = MutableProgressData().apply {
            mCurrent = 23L
            mTotal = 100L
            mAction = action1
            mTimePassed = 2000L
            mCurrentData = data1
        }

        val pdi = pd as ProgressData
        assertTrue(pdi.current == 23L)
        assertTrue(pdi.total == 100L)
        assertTrue(pdi.action ==action1)
        assertTrue(pdi.currentData == data1)

        pd.apply {
            mCurrent = 2L
            mTotal = 200L
            mAction = action2
            mTimePassed = 4000L
            mCurrentData = data2
        }

        assertTrue(pdi.current == 2L)
        assertTrue(pdi.total == 200L)
        assertTrue(pdi.action ==action2)
        assertTrue(pdi.currentData == data2)

    }


}