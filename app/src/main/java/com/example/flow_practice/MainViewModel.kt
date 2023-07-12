package com.example.flow_practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    // Flow에서 발행하고자 하는 데이터의 타입을 명시
    val countDownFlow = flow<Int> {
        // FlowCollector
        // inside of this block, we can emit a value
        // kind of coroutine scope => can execute suspend functions in it

        // starting value
        val startingValue = 10
        var currentValue = startingValue

        // without this code, the timer would start with 9
        emit(startingValue)

        // loop is needed... for counting down
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            // 'notify' the UI about the change by using emit()
            // here we can put the integer value that we used to declare a flow value
            emit(currentValue)
        }
    }

    // as the viewmodel is initialized
    init {
        collectFlow()
    }

    // compose state 변수를 사용하지 않는 경우, collect 메소드 이용해서 값을 받아볼 수 있음
    // UI Layer에서는 collect() 사용하는 것은 지양. 더 쉬운 collectAsState를 사용
    /*private fun collectFlow() {
        viewModelScope.launch {
            countDownFlow.collect { time ->
                delay(1500L)
                println("The current time is $time")
                // 결과 : 1.5초마다 한 번씩 출력
            }
        }
    }*/

    private fun collectFlow() {
        viewModelScope.launch {
            countDownFlow.collectLatest { time ->
                // 1초마다 한번씩 값이 방출되는데, 블록 내부의 코드가 모두 실행되는데는 1.5초가 걸림.
                // 즉, 방출되는 데 걸리는 시간 < 코드가 실행되는 데 걸리는 시간
                // 따라서 마지막 한번만 실행됨
                delay(1500L)
                println("The current time is $time")
                // 결과 : The current time is 0 단 한 번만 방출
            }
        }
    }

}