package com.darkraha.services.core.utils

import java.lang.ref.SoftReference

open class SoftRef<T>(obj: T, size: Int = 0, sizeCalc: ((T) -> Int)? = null) : SoftReference<T>(obj) {

    var objSize = 0
        protected set

    var sizeCalculator: ((T) -> Int)? = null
        private set

    init {
        objSize = size
        sizeCalculator = sizeCalc
        sizeCalculator?.apply {
            objSize = invoke(obj)
        }
    }

    open fun recalcSize(): Int {
        sizeCalculator?.takeIf { get() != null }?.apply {
            objSize = invoke(get()!!)
        }
        return objSize
    }

}