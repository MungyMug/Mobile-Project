package com.example.mobileapp.ui.nav

object Routes {
    const val MENU = "menu"
    const val CAMERA = "camera"
    const val RESULT = "result"
    const val GALLERY = "gallery"
    const val DETAIL = "detail/{entryId}"

    fun detail(entryId: Int) = "detail/$entryId"
}
