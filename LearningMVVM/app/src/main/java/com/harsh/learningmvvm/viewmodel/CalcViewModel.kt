package com.harsh.learningmvvm.viewmodel
import androidx.lifecycle.ViewModel
import com.harsh.learningmvvm.model.CalcModel

class CalcViewModel: ViewModel() {
    fun calc(num1: Int,num2: Int): CalcModel{
        val sum = num1 + num2
        return CalcModel(num1,num2,sum)
    }
}