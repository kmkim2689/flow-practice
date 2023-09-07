## Kotlin Flow Practice

* The repository for practicing Flow of Kotlin mainly used with Coroutines.

* In this repository, the things I'm gonna post are...
  * What is Flow?
  * How to use Flow?
  * How to observe values from Flow?
  * What are Flow Operators?
  * etc...

* My references
  * Flow Basics - The Ultimate Guide to Kotlin Flows (Part 1) by Philipp Lackner
    > https://youtu.be/ZX8VsqNO_Ss
  * Android Developers
    > https://developer.android.com/kotlin/flow

---

### What is a Flow?
* the definition by Android Developers official document
  > In coroutines, a flow is a type that can **emit multiple values sequentially**, as opposed to suspend functions that return only a single value.
  
* Related deeply with Reactive Programming = Notified about the changes!!!
  * Flow is the Kotlin language feature that serves as a reactive programming framework
  * being **notified changes** in the code + **doing something** with these changes
    * notified the changes and with them update UI layer
  * Think of the assembly line to produce a cellphone... moving forward, doing something with some 'steps'
    * (step 1) give the color -> (step 2) package -> (step 3) put it in a big box...
    * these steps are done **sequentially**.
  * In Kotlin Flow, when making an API request,
    * (step 1) get the response -> (step 2) **map** the response to something else(to display on the UI) -> (step 3) filter specific things from the list -> (step 4) show on the UI
  * That is, Flow is some kind of pipelines of actions that we apply before displaying on the UI

* A Flow is in the end, the Coroutine that can emit multiple values over a period of time
  * A single Coroutine, A single suspend function only can return a single value
  * That is why Flow is used
    * For example, when making a countdown timer application
    * should emit a value every single second.
    * cannot do it with a normal coroutine
    * reason : how to get notified about specific value every single second?? no idea
    
* Kotlin Flow Example : Countdown Timer Application
  * needed for countdown timer
    * initial time
    * counting down one second
    * notify the change to UI
    
  * How to make a Flow?
    
    * flow variable

       ```
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
       ```
  
    * get value from flow variable
    
      * UI Layer : collectAsState

      ```
      val time = viewModel.countDownFlow.collectAsState(initial = 10)
      Box(modifier = Modifier.fillMaxSize()) {
        Text(
          text = time.value.toString(),
          fontSize = 30.sp,
          modifier = Modifier.align(Alignment.Center)
        )
      }
      ```
   
      cf) Why initial value should be set while using collectAsState?
      * to ensure that consumer can make use of the flow even nothing was emitted from the producer
      * If nothing was emitted from the flow or nothing is going to be emitted from the flow(null), the initial value is set for that variable so that consumer can make use of this
      * By the time the first value is emitted from the flow, the initial value will be the state value of the variable

      * Business Logic : collect / collectLatest


  * collect vs collectLatest
    * collect : 모든 발행에 대해 실행됨. 즉 flow변수의 값이 변경될 때마다 반영
    * collectLatest : 똑같이 모든 단일 발행에 대해 실행된다는 점에서는 collect와 동일
      * 다만, collectLatest는 단일 발행 시 실행되는 블록에서 구현한 코드가 모두 수행되는 데 오랜 시간이 걸리는 동안(즉 아직 코드의 실행이 끝나지 않은 상태에서)
      * 새로운 발행이 들어오게 된다면, 블록 내부의 코드 실행은 취소된다. 새로운 발행에 대해 다시 코드가 실행된다.

---

### Flow Operators
  * to transform the emissions
  * mainly used operators
    * collect / collectLatest : 데이터를 발행받음 
    * filter
      * 조건을 만족하는 데이터만 발행 -> 블록 구현부 내부에는 boolean expression
      * true인 것만 발행받을(collect) 수 있음
    * map
      * 발행되는 각 데이터 하나하나를 같은 기준으로 변경
      * 발행값을 받아 그 발행값을 가지고 다른 값으로 변형한 것을 발행
    * onEach
      * 하나하나 순회한다는 점에서 map와 동일
      * 다만, doesn't really transform the values 
      * 예시 : 단순 출력
      * 그러면 collect와 다를 바가 없는 것인가? <No>
        * onEach는 다시 Flow를 return한다. -> 이후에 다른 operator 사용 가능
          ```
          /**
          Returns a flow that invokes the given [action] **before** each value of the upstream flow is emitted downstream.
          **/
            public fun <T> Flow<T>.onEach(action: suspend (T) -> Unit): Flow<T> = transform { value ->
            action(value)
            return@transform emit(value)
          }
          ```

        * 반면 collect는 아무것도 return하지 않음. -> collect 쓰면 끝.
          ```
          public suspend fun collect(collector: FlowCollector<T>)
          ```

      * 둘은 같은 결과
        ```
        countDownFlow.onEach {
          println(it)
        }.launchIn(viewModelScope)
        ```
     
        ```
        viewModelScope.launch {
          countDownFlow.collect {
            println(it)
          }
        }
        ```

  * terminal operators : terminate the flow - take the whole results of a flow -> all emissions together and then do something with these
    * count : 발행되는 것들 중 특정 조건에 맞는 값의 수를 카운트
      쓰는 순간 flow는 종료되며, return하는 것이 Flow가 아닌 Int.
      즉, 마지막에 count를 쓰는 flow라면 어떤 변수에 할당되어야 함.

```
      private fun collectFlow() {
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
      }
```

    * reduce
      * every single emission
      * parameter : accumulator, value
      * accumulator : 이전까지 거쳐온 값들에 대한 연산 결과
      * value : 현재 값
      * 최종적으로 반환되는 것 : 마지막 순회에서 accumulator와 value에 대한 연산
        
    * fold
      * 기본적으로 reduce와 유사
      * 다만 reduce와 달리, 시작 accumulator값을 제공해주어야 함.(initial value)
      * 초기값을 설정해주는 것 -> 초기 accumulator의 값을 설정해주는 것.

  * Flattening Operators
    * What is Flattening?
      * ex) list of lists to single list
        [[1, 2], [1, 2, 3]] -- flattening --> [1, 2, 1, 2, 3]
      * 위와 같은 예시로 비유는 가능 -> 실제로 list를 flow로 대입하여 생각하면 됨
      * **여러 개의 Flow를 합쳐 하나의 Flow**로 만드는 것을 Flattening한다고 보면 됨.
    
    * flatMap
      * flatMapConcat : 여러개의 flow를 하나의 flow로 이어붙임
      * flatMapMerge : 거의 사용되지 x
        * flatMapConcat과의 차이
        * 합쳐진 flow들이 동시에 실행되는 것이 Merge
        * 합쳐진 순서대로 하나하나 실행되는 것이 Concat
      * flatMapLatest : collectLatest의 원리와 비슷
        * 앞의 것이 emit되는 동안 새로운 flow에 대한 요청이 들어오면 중단하고 새로운 것에 대해 emit 실시

  * buffer

    ```
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
            .buffer()
            .collect {
                println("Flow : eating $it")
                // 먹는데 걸리는 시간
                delay(1500L)
                println("Flow : finished eating $it")
            }
        }
        
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
        
    }
    ```

  * conflate : 'skip entirely' if previous collect is not finished
  * collectLatest : 'suspend and skip' if previous collect is not yet finished

---

### NormalFlow vs StateFlow vs SharedFlow
* StateFlow - kind of Hot Flow
  * used to keep state in a Flow
  * just like a livedata **without the lifecycle awareness** so, an Activity cannot detect
  * the state flow can't detect when the activity goes in the background
  * Collect 이전에 발행된 값들은 알 수 없음
  * change -> stateflow exactly keep one single value -> Hot Flow
    변화가 발생하면, 이전의 값은 사라지고 오직 하나의 값만을 보유
  * 다만, Compose 사용 시 stateFlow 사용은 권장하지 않음. -> compose에 collectAsState기능이 이미 존재하기 때문
  
* Hot Flow vs Cold Flow
  * Cold Flow : if there are no collectors, the flow won't do anything
  * Hot Flow : Even if there no collectors the flow will do something
    * if we assign a new value to stateFlow, can change that value even if there are no collectors

* SharedFlow - Hot Flow
  * used to send one-time event
  * two types of emissions
    * state emission : with StateFlow
    * event emission : with SharedFlow
      * only receive a single time
      * ex) showing snackbar, login successful => must only once
  * can work with multiple collectors
  * with **replay**, can cache
