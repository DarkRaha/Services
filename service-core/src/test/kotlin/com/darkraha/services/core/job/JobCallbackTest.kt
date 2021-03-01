package com.darkraha.services.core.job

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

internal class JobCallbackTest{

    @Test
    fun testCreateWithOwner(){
        val weakOwner = WeakReference(null)

      val cb =  JobCallbackState<String>(JobState.FINISHED, weakOwner, false){

        }

        assertTrue(cb.owner == weakOwner)
        assertFalse(cb.isOwnerAllowed())

    }

}