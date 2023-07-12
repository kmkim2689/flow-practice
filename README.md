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
  
    * get value from flow variable
    
      * UI Layer : collectAsState

            val time = viewModel.countDownFlow.collectAsState(initial = 10)
              Box(modifier = Modifier.fillMaxSize()) {
              Text(
                text = time.value.toString(),
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center)
              )
            }

      * Business Logic : collect / collectLatest


  * collect vs collectLatest
    * collect : 모든 발행에 대해 실행됨. 즉 flow변수의 값이 변경될 때마다 반영
    * collectLatest : 똑같이 모든 단일 발행에 대해 실행된다는 점에서는 collect와 동일
      * 다만, collectLatest는 단일 발행 시 실행되는 블록에서 구현한 코드가 모두 수행되는 데 오랜 시간이 걸리는 동안(즉 아직 코드의 실행이 끝나지 않은 상태에서)
      * 새로운 발행이 들어오게 된다면, 블록 내부의 코드 실행은 취소된다. 새로운 발행에 대해 다시 코드가 실행된다.

* Cold Flow vs Hot Flow