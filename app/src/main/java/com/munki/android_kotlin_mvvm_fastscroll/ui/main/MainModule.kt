package com.munki.android_kotlin_mvvm_fastscroll.ui.main

import androidx.databinding.ObservableArrayList
import com.munki.android_kotlin_mvvm_fastscroll.GlobalApplication
import dagger.Module
import dagger.Provides

/**
 * Inject with ViewModel
 * @author 나비이쁜이
 * @since 2020.10.05
 */
@Module
class MainModule {

    @Provides
    fun createViewModel(application: GlobalApplication): MainViewModel {
        return MainViewModel(application, makeWordList())
    }

    /**
     * 단어 리스트 생성
     */
    private fun makeWordList(): ObservableArrayList<String> {
        val wordList = ObservableArrayList<String>()
        wordList.add("가")
        wordList.add("나")
        wordList.add("다")
        wordList.add("라")
        wordList.add("마")
        wordList.add("바")
        wordList.add("사")
        wordList.add("아")
        wordList.add("자")
        wordList.add("차")
        wordList.add("카")
        wordList.add("타")
        wordList.add("파")
        wordList.add("히")
        wordList.add("하")
        wordList.add("호")
        wordList.add("a")
        wordList.add("b")
        wordList.add("c")
        wordList.add("d")
        wordList.add("e")
        wordList.add("f")
        wordList.add("g")
        wordList.add("h")
        wordList.add("i")
        wordList.add("j")
        wordList.add("k")
        wordList.add("l")
        wordList.add("m")
        wordList.add("n")
        wordList.add("p")
        wordList.add("q")
        wordList.add("r")
        wordList.add("s")
        wordList.add("t")
        wordList.add("u")
        wordList.add("v")
        wordList.add("w")
        wordList.add("x")
        wordList.add("y")
        wordList.add("z")
        return wordList
    }
}