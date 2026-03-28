package com.kanyandula.nyasa.ui

interface DataStateChangeListener {
    fun displayProgressBar(isLoading: Boolean)
    fun displayErrorDialog(message: String)
    fun displayErrorToast(message: String)
    fun displaySuccessDialog(message: String)
    fun displayToast(message: String)
    fun expandAppBar()
    fun hideSoftKeyboard()
    fun isStoragePermissionGranted(): Boolean
}
