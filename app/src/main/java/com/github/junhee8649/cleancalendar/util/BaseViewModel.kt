package com.github.junhee8649.cleancalendar.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

open class BaseViewModel : ViewModel() {
    val crashPreventionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
}
