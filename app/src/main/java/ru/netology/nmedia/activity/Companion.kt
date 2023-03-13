package ru.netology.nmedia.activity

import android.os.Bundle
const val POST_TEXT: String = "post text input"
const val POST_KEY = "POST_KEY"
class Companion {

    companion object{
        var Bundle.textArg:String?
            set(value) = putString(POST_KEY, value)
            get() = getString(POST_KEY)
    }
}