import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class RxKotlinOperator {

    fun run() {
        flatMapAndConcatMap()
    }

    /**
        - filter - 기존의 collection의 filter와 동일
        - debounce
            - 빠르게 데이터가 생산되는 경우 특정 시간이상 데이터 생산 간격이 벌어지는 경우에만 값을 출력합니다.
            - 예를 들어 네이버 검색창에서 원하는 문구를 타이핑하면 타이핑 하는 동안 추천검색어를 하단에 보여줍니다.
              이때 "고구마"를 검색한다고 가정하면, "ㄱ" 과 "ㅗ"는 따로 타이핑 되지만 두개의 추천검색어를 각각 보여줄 필요는 없습니다.
              아마도 빠르게 타이핑해서 지나가기 때문이죠.
              따라서 입력 간격이 특정 시간 이상 벌어지는 경우에만 값을 배출하도록 하는게 debounce 연산자의 역할 입니다.
            - 주의사항: debounce operator 자체가 값을 스스로 누적하지 않으므로 주의해야합니다!
        - distict | distictUntilChanged
            - DB SQL애서 중복 데이터를 걸러낼때 사용하는것처럼, 출력하는 모든 연산자를 기억하고 있다가 이전 출력값과 동일한 값이 배출되면 아이템을 걸러냄
            - distictUntilChanged 연산자는  바로 직전연산자와 동일한지만 판단하여 걸러냅니다
        - elementAt | ignoreElements
            - elementsAt은 배열 연사낮와 동일하기 특정 순서에 배출되는 데이터를 획득할 수 있습니다.
            - 단, 앞단의 데이터가 모두 배출되고 나서 지정한 순서가 올때까지 기다려야 합니다.
            ex) val ob = Observable.range(1, 50)
                ob.elementAt(3).subscribe{ println("element 3nd $it") }
                ob.elementAt(100).subscribe{ println("element 3nd $it") } // range 가 50까지이므로 실행을 안하고 무시함
                ob.ignoreElements().subscribe{ println("emission completed") }
            - 만약 데이터의 배출엔 관심없고, 배출이 완료 되었는지에 대한 판단만 필요하다면 ignoreElements 연산자를 이용할 수 있습니다.
            - ignoreElements 연산자는 onComplete가 호출될 때 값을 전달합니다.
        - first | last
            - collection에서 사용하는 연산자와 동일하게 첫번째 값이나, 마지막 값을 반환합니다.
            - 단 param으로 기본값을 넣어야 하며, first 나 last 값이 없을경우 param으로 전달받은 기본값을 반환합니다.
        - take | cast
            - take는 앞에서 부터 특정 개수만큼만 수신하고 나머지는 무시합니다
            - cast는 배출된 타입의 클래스를 다른 타입 클래스로 casting 할때 사용합니다.
        - flatMap | concatMap
            - 이 두개의 operator는 Observable이 Observable을 방출할 때, flatten해주는 작업을 합니다.
            - flatMap과 concatMap은 내부적으로 merge와 concat을 사용하기 때문에 flatMap은 각각 Observable의 속도에 따라 방출되고, concatMap은 순서를 보장하며 방출됩니다.
              이 둘은 coroutine Flow의 flatMapMerge 와 flatMapConcat과 동일하게 동작합니다
            - List<List<Int>> -> Observable<Observable<Int>> or Flowable<Flowable<Int>>
            - flatMap은 flatMap 블럭의 Observable을 병합하여 한방에 방출하기 때문에 각각의 delay에 따라 방출됩니다.
            - 단, concatMap은 순서를 보장하면서 방출하기 때문에 순서대로 delay만큼 대기한 후 다음값을 방출합니다.
        - defaultIfEmpty | switchIfEmpty
            - filter와 debounce등 특정 조건에 맞는 값을 출력하는경우 조건을 맞추지 못하면 출력값이 하나도 없는경우도 발생합니다.
            - 이때 기본값을 설정하는 defaultInEmpty와 기본 Observable을 설정할 수 있도록 하는 switchIfEmpty 연산자를 제공합니다.
        - startWith
            - 생산자의 맨 앞에 값을 추가하는 역할을 합니다.
        - sorted
            - 정렬을 위해 sorted 연산자를 사용할 수 있습니다.
                다만 이 연산자는 모든 방출을 저장했다가, 방출이 완료되면 정렬하기 때문에 시간, 성능, 메모리의 단점이 존재합니다.
                너무 느린 방출인 경우 정렬을 위해 방출의 마지막까지 기다려야 하며, 대규모의 방출인 경우 OOM 발생 여지가 있습니다.
        - scan | reduce
            - scan은 방출시점에 방출된 값과 직전 이전값을 같이 넘겨 받습니다
            - 따라서 방출되는 값을 누적시켜서 적용하는 용도로 사용하기 유리하며, reduce의 경우 방출되는 값과 이전값을 누적연산하여 마지막 방출때 하나의 값만을 전달 받습니다.
                (reduce는 collection의 reduce와 쓰임과 활용이 동일합니다.)
            - reduce는 onComplete()가 호출되면 누적된 최종값을 출력합니다
        - toMap | toMultimap
            - 배출된 정보를 모두 모아 map으로 출력할수 있습니다.
                이때 toMap을 사용하면, key와 value를 지정해야하고 동일key에 여러 값을 set하게 되면 마지막값만 저장됩니다.
                또한 toMultimap을 사용하면 특정 key로 grouping 할 수 있습니다.
        - toList | toSortedList
            - 위와 유사함
        - zip | apiWith
            - zip 연산자는 두개 이상의 Observable(flowable)을 병합 합니다.
            - 각각의 Observable에서 방출하는 값에 대해서 pair를 맞춰서 방출하기 때문에 Observable의 방출 개수가 맞지 않으면, 가장 작은 개수의 Observable에 출력이 맞춰 집니다.
              즉! 짝이 없으면 버려 집니다
            - zip은 Observable(Flowable)에서 징줜하는 static Method로 최대 9개까지 병합할 수 있습니다.
            - zip은 pair를 맞추어 출력되기 때문에 한쪽의 출력이 느리다면 느린쪽에 맞춰 집니다.
            - zipWith는 coroutine flow의 zip 함수와 동일하게 동작합니다
        - combineLastest
            - 두개 이상의 Observable(Flowable)을 병합하여 출력합니다
            - Observable중 하나라도 방출되면 나머지 Observable의 최신값을 가지고 병합하며, 서로 방출 속도가 달라 다른 observable이 아직 방출된 값이 없는 상태에서 방출하면, 해당 방출은 버려집니다.
        - merge | mergeWith | mergeArray, concat | concatWith, concatArray
            - merge: Observable의 static 함수로 최대 4개까지 병합
            - mergeArray: Observable의 static 함수로 args로 인자를 받아 다수의 Observable 병합 가능
            - mergeWith: Observable의 instance 객체를 이용하여 다른 Observable과 병합
            - concat은 merge와 비슷하지만 Observable을 병합하는 작업을 하지만 concat은 선언된 순서를 보장하여 각 Observable을 이어 붙입니다.
        - amb | ambArray
            - 여러개의 Observable중 가장 빠르게 시작하는 Observable만 사용하고 나머지 Observable의 방출은 버립니다.
            - 동일한 소스가 여러 서버에 퍼져 있을때 동시에 호출하고 가장 빨리 응답하는 서버의 응답만을 처리하거나, Android에서 여러가지 위치 측정 방법중 가장 빨리 응답하는 결과를 사용할때 유용 하겠네요.
        - groupby
            - 이전의 Multimap으로 groupping 된 map을 만들었었는데, observable을 Map이나 Collection으로 만들어 반환하는건 바람직하지 않습니다.
            - Groupping하고 결과로도 Observable을 반환하는 연산자가 groupby이다
        - skip | skipLast | skipWhile | skipUntil
            - 방출시점에 조건에 따라 방출을 전달하지 않음
        - take | takeLast | takeWhile | takeUntil
            - 특정 조건에 따라 방출된 데이터를 획득
        - onErrorReturnItem | onErrorReturn
            - 에러가 발생 시 특정값으로 교체하여 전달, Observable의 생산은 중단
        - onErrorResumeNext
            - Error 발생시 다른 Observable을 구독하도록 onErrorResumeNext를 사용할 수도 있습니다.
        - retry
            - error 발생시 재시도 합니다.
            - 보통 네트워크 요청후 응답을 받아올 경우 실패시 "X번 재시도 한다"라는 구현상황을 요청받습니다.
            - 여기서는 명확하게 계속 에러를 발생 시키지만 "네트워크 실패시 세번 재시도 한다" 같은 시나리오를 구현 할 때 사용하기 적합한 연산자 입니다.
     */

    fun debounce() = runBlocking {
        val observable = Observable.create<String>{
            runBlocking {
                it.onNext("고")
                it.onNext("고구")
                it.onNext("고구마")
                delay(300)
                it.onNext("고구마 맛")
                it.onNext("고구마 맛있")
                it.onNext("고구마 맛있게")
                delay(300)
                it.onNext("고구마 맛있게 먹")
                it.onNext("고구마 맛있게 먹는")
                it.onNext("고구마 맛있게 먹는법")
                it.onComplete()
            }
        }

        observable
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe{ println(it) }
    }

    fun take() = runBlocking {
        val ob = Observable.range(1, 10)

        ob.take(4).subscribe { println(it) }
    }

    fun cast() = runBlocking<Any> {
        val ob = Observable.range(1, 10)

        ob.cast(Number::class.java).subscribe { println(it) }
    }

    private fun createSlowObservable(startNum: Int): Observable<Int> {
        val endNum = startNum + 10
        return Observable.create<Int> {
            for(i in startNum until endNum){
                it.onNext(i)
                runBlocking { delay(100) }
            }
            it.onComplete()
        }
    }

    fun switchInEmptyAndDefaultIfEmpty() = runBlocking {
        val ob1 = createSlowObservable(1)
        val elapsedTime1 = measureTimeMillis {
            ob1.filter{ it == 20 }
                .defaultIfEmpty(-1)
                .subscribe{ println("received: $it") }
        }

        println("elapsed time1: $elapsedTime1")

        val ob2 = createSlowObservable(11)
        val elapsedTime2 = measureTimeMillis {
                ob2.switchIfEmpty(ob2)
                    .filter{ it == 20 }
                    .defaultIfEmpty(-1)
                    .subscribe{ println("received: $it") }
        }

        println("elapsed time2: $elapsedTime2")
    }

    fun sorted() {
        val ob = Observable.just(1,3,5,7,8,2,4,6,8,10)
        println("sort()")
        ob.startWith(0).sorted().subscribe{ print("$it, ") }
        println()
        ob.startWith(0).sorted{ num1, num2 -> if(num1 >= num2) -1 else 1 }.subscribe{ print("$it, ") }
    }

    private fun scanTest(): Observable<String> {
        return Observable.create<String> {
            runBlocking {
                it.onNext("고")
                it.onNext("구")
                it.onNext("마")
                it.onNext(" ")
                delay(300)
                it.onNext("맛")
                it.onNext("있")
                it.onNext("게")
                it.onNext(" ")
                delay(300)
                it.onNext("먹")
                it.onNext("는")
                it.onNext("법")
                it.onNext(" ")
                delay(300)
                it.onComplete()
            }
        }
    }

    fun scanAndReduce() = runBlocking{
        val ob = scanTest()

        println("------- scan() -------")
        ob.scan{ prev, next -> "$prev$next"}.subscribe{ println(it)}
        println("------- reduce() -------")
        ob.reduce{ prev, next -> "$prev$next"}.subscribe{ println(it)}
        println("------- scan with debounce() -------")
        ob.scan{ prev, next -> "$prev$next"}.debounce(200, TimeUnit.MILLISECONDS).subscribe{ println(it)}
    }

    fun toMapAndToMultimap() = runBlocking<Unit>{
        val ob = Observable.range(1, 10)

        ob.toMap( {key -> key}, {value -> value * value} ).subscribeBy{ println(it) }

        ob.toMultimap {
            if(it % 2 == 0) {
                "even"
            }else{
                "odd"
            }
        }.subscribeBy { println(it) }
    }

    fun zipAndZipWith() {
        val obj1 = (1 until 5).toObservable()
        val obj2 = Observable.just("one", "two", "three", "four")

        println("-------- zip() --------")
        Observable.zip(obj1, obj2, BiFunction {a: Int, b: String -> "$a: $b"}).subscribe{ println(it) }

        println("-------- zipWith() --------")
        obj1.zipWith(obj2, BiFunction {a: Int, b: String -> "$a: $b"}).subscribe{ println(it) }
    }

    fun combineLatest() = runBlocking {
        val obj1 = Observable.interval(100, TimeUnit.MILLISECONDS)
        val obj2 = Observable.interval(250, TimeUnit.MILLISECONDS)

        val time = System.currentTimeMillis()
        obj1.subscribe{ println("obj1 emit: $it") }
        obj2.subscribe{ println("obj2 emit: $it") }

        Observable.combineLatest(obj1, obj2, BiFunction{a: Long, b: Long -> "Time:${System.currentTimeMillis()-time} obj1: $a obj2: $b"}).subscribe{ println(it) }

        delay(500)
    }

    fun mergeObservable() = runBlocking {
        val obj1 = Observable.just(1, 2)
        val obj2 = Observable.just(3,4)

        Observable.merge(obj1, obj2).subscribe{ println(it) }
    }

    fun amb() = runBlocking{
        val obj1 = Observable.just(1,2).map {
            runBlocking { delay(100)}
            it
        }
        val obj2 = Observable.just(3,4)

        Observable.amb(listOf(obj1, obj2)).blockingSubscribe{ println("amb: $it") }
        Observable.ambArray(obj1, obj2).blockingSubscribe{ println("ambArray: $it") }
    }

    fun groupby() {
        val obj1 = Observable.range(1, 10)

        obj1.groupBy { it % 2 == 0 }.subscribe{ keySet ->
            val key = keySet.key
            keySet.subscribe{
                println("key: $key, value: $it")
            }
        }
    }

    fun flatMapAndConcatMap() {
        val obj1 = Observable.range(1, 5)
        println("--------- flatMap() ---------")
        val elapsedTime1 = measureTimeMillis {
            obj1.flatMap { flat -> getDelayedObservable(flat) }
                .blockingSubscribe{ println(it) }
        }
        println("Elapsed Time: $elapsedTime1")
        println("--------- concatMap() ---------")
        val elapsedTime2 = measureTimeMillis {
            obj1.concatMap { flat -> getDelayedObservable(flat) }
                .blockingSubscribe{ println(it) }
        }
        println("Elapsed Time: $elapsedTime2")
    }

    private fun getDelayedObservable(value: Int): Observable<String> {
        val delay = Random.nextInt(100)
        return Observable.just(value)
            .map { "Delay:$delay - $it" }
            .delay(delay.toLong(), TimeUnit.MILLISECONDS)
    }
}