package com.example.flow_practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
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

    // stateflow
    private val _stateFlow = MutableStateFlow(0)
    // to collect changes from _stateFlow
    val stateFlow = _stateFlow.asStateFlow()

    // sharedFlow -> initial value not needed : event이므로
    private val _sharedFlow = MutableSharedFlow<Int>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    // as the viewmodel is initialized
    init {
//        collectFlow()
        // nothing happens as it is a hot flow
        // squareNum(3)
        viewModelScope.launch {
            sharedFlow.collect {
                delay(2000L)
                println("FLOW1 : The received num is $it")

            }
        }

        // only first flow prints the result
        // squareNum(3)

        viewModelScope.launch {
            sharedFlow.collect {
                delay(3000L)
                println("FLOW2 : The received num is $it")

            }
        }

        // both print the result
        squareNum(3)
    }

    // sharedFlow emit는 단 한 번만 수행 가능.
    fun squareNum(number: Int) {
        viewModelScope.launch {
            _sharedFlow.emit(number * number)
        }
    }


    fun incrementCounter() {
        _stateFlow.value += 1
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

    /*private fun collectFlow() {
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
    }*/

    // filter operator
    /*private fun collectFlow() {
        viewModelScope.launch {
            countDownFlow
                .filter { time ->
                    // boolean expression
                    time % 2 == 0
                }
                .collect { time ->
                println("The current time is $time")
            }
        }
    }*/

    /*
    The current time is 10
    The current time is 8
    The current time is 6
    The current time is 4
    The current time is 2
    The current time is 0
     */


    // map operator
    /*private fun collectFlow() {
        viewModelScope.launch {
            countDownFlow
                .filter { time ->
                    // boolean expression
                    time % 2 == 0
                }
                .map { time ->
                    // take the time value(for here, filtered value)
                    // and use that to manipulate each value to another value
                    time * time
                }
                .onEach { time ->
                    println(time)
                }
                .collect { time ->
                    println("The current time is $time")
                }
        }
    }*/

    /*
    The current time is 100
    The current time is 64
    The current time is 36
    The current time is 16
    The current time is 4
    The current time is 0
     */

    // terminal operators - count
    /*private fun collectFlow() {
        viewModelScope.launch {
            val count = countDownFlow
                .filter { time ->
                    // boolean expression
                    time % 2 == 0
                }
                .map { time ->
                    // take the time value(for here, filtered value)
                    // and use that to manipulate each value to another value
                    time * time
                }
                .onEach { time ->
                    println(time)
                }
                .count {
                    // count를 쓰는 순간 flow는 종료되며, return하는 것이 flow가 아닌 Int.
                    it % 2 == 0
                }

            println("count is $count")
        }
    }*/
    /*
    count is 6
     */

    // terminal operators - reduce
    /*private fun collectFlow() {
        viewModelScope.launch {
            val reduceResult = countDownFlow
                .reduce { accumulator, value ->
                    // two values, accumulator and value
                    // reduce : executed every single emission
                    // accumulator : 이전 reduce 연산 결괏값
                    // value : 현재 순회 중인 요소

                    // accumulator에 들어가게 될 값
                    accumulator + value
                    // 순회를 모두 마치면, 마지막 accumulator와 value의 값을 더한 result가 되어 반환된다.

                    *//*
                    fun main(args: Array<String>) = runBlocking<Unit> {
                        val sum = (1..5).asFlow()
                            .reduce { accumulator, value -> accumulator + value }

                        println(sum)
                    }

                    accumulator [1] value [2]
                    accumulator [3] value [3]
                    accumulator [6] value [4]
                    accumulator [10] value [5]
                    result [15]
                     *//*
                }
            println("result : $reduceResult") // result : 55
        }
    }
    */

    // fold
    /*private fun collectFlow() {
        viewModelScope.launch {
            val foldResult = countDownFlow
                .fold(100) { accumulator, value ->
                    accumulator + value
                }
            println("result : $foldResult") // result : 155. 100(initial value) + 55(reduce 연산)
        }
    }*/

    // flattening - flatMapConcat
    /*private fun collectFlow() {
        val flow1 = flow {
            emit(1)
            delay(500L)
            emit(2)
        }


        viewModelScope.launch {
            flow1.flatMapConcat { value ->
                // int value(flow1이 방출하는 값 1, 2)
                // return a flow! -> create another flow
                flow {
                    // returning flow emits new value based on the value flow1 emits
                    emit(value + 1)
                    delay(500L)
                    emit(value + 2)
                }
            }.collect { value ->
                println("The value is $value")
                *//*
                map1
                The value is 2 (1 + 1)
                The value is 3 (1 + 2)
                ----------------------
                map2
                The value is 3 (2 + 1)
                The value is 4 (2 + 2)
                 *//*
            }
        }
    }*/

    // flatMapMerge
    /*private fun collectFlow() {
        val flow1 = flow {
            emit(1)
            delay(500L)
            emit(2)
        }


        viewModelScope.launch {
            flow1.flatMapMerge { value ->
                // int value(flow1이 방출하는 값 1, 2)
                // return a flow! -> create another flow
                flow {
                    // returning flow emits new value based on the value flow1 emits
                    emit(value + 1)
                    delay(500L)
                    emit(value + 2)
                }
            }.collect { value ->
                println("The value is $value")
                *//*
                map1
                The value is 2 (1 + 1)
                The value is 3 (1 + 2)
                ----------------------
                map2
                The value is 3 (2 + 1)
                The value is 4 (2 + 2)
                 *//*
            }
        }
    }*/

    // buffer의 필요성 : 만약 일반적으로 구현한 flow라면...
    /*private fun collectFlow() {
        val flow = flow {
            // 레스토랑에서 음식을 기다림
            delay(200L)
            // 에피타이저 나옴
            emit("appetizer")
            delay(1000L)
            emit("main dish")
            delay(100L)
            emit("desert")
        }

        viewModelScope.launch {
            flow.onEach {
                println("FLOW : $it is delivered")
            }
                .collect {
                    println("Flow : eating $it")
                    // 먹는데 걸리는 시간
                    delay(1500L)
                    println("Flow : finished eating $it")
                }
        }
        *//*
        그냥 순차적으로 요리, 배달, 섭취, 섭취 완료가 음식별로 진행됨
        하나의 요리 이외에는 다른 요리는 진행되지 않음.
        FLOW : appetizer is delivered
        Flow : eating appetizer
        Flow : finished eating appetizer
        FLOW : main dish is delivered
        Flow : eating main dish
        Flow : finished eating main dish
        FLOW : desert is delivered
        Flow : eating desert
        Flow : finished eating desert

        이유 : delay와 emit, collect 모두 suspend 함수이기 때문
         *//*
    }*/

    // using buffer
    /*private fun collectFlow() {
        val flow = flow {
            // 레스토랑에서 음식을 기다림
            delay(200L)
            // 에피타이저 나옴
            emit("appetizer")
            delay(1000L)
            emit("main dish")
            delay(100L)
            emit("desert")
        }

        viewModelScope.launch {
            flow.onEach {
                println("FLOW : $it is delivered")
            }
                // emit되는 것들에 대해 collect가 각기 다른 코루틴에서 실행되도록 저장함
                // 앞의 collect의 실행이 완료되지 않아도, 같이 진행
                .buffer()
                .collect {
                    println("Flow : eating $it")
                    // 먹는데 걸리는 시간
                    delay(1500L)
                    println("Flow : finished eating $it")
                }
        }
        *//*
        식사가 더 빠르게 진행되었음
        FLOW : appetizer is delivered
        Flow : eating appetizer
        FLOW : main dish is delivered
        FLOW : desert is delivered
        Flow : finished eating appetizer
        Flow : eating main dish
        Flow : finished eating main dish
        Flow : eating desert
        Flow : finished eating desert
         *//*
    }*/

    // using conflate
    // difference to buffer : skip emitted value
    // collect가 전부 완료되지 않은 상태에서 다음 것이 emit되어 collect 요청받은 상황이라면,
    // 그 emit된 것은 skip한다.
    private fun collectFlow() {
        val flow = flow {
            // 레스토랑에서 음식을 기다림
            delay(200L)
            // 에피타이저 나옴
            emit("appetizer")
            delay(1000L)
            emit("main dish")
            delay(100L)
            emit("desert")
        }

        viewModelScope.launch {
            flow.onEach {
                println("FLOW : $it is delivered")
            }
                // emit되는 것들에 대해 collect가 각기 다른 코루틴에서 실행되도록 저장함
                // 앞의 collect의 실행이 완료되지 않아도, 같이 진행
                .conflate()
                .collect {
                    println("Flow : eating $it")
                    // 먹는데 걸리는 시간
                    delay(1500L)
                    println("Flow : finished eating $it")
                }
        }
        /*
        FLOW : appetizer is delivered
        Flow : eating appetizer
        FLOW : main dish is delivered
        FLOW : desert is delivered
        Flow : finished eating appetizer
        Flow : eating desert
        Flow : finished eating desert
         */
    }


}