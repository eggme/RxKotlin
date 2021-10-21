import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.math.log
import kotlin.system.measureTimeMillis

class RxKotlinApp

fun main() {
    // https://thdev.tech/kotlin/2018/11/07/RxJava-To-Kotlin-Coroutine/
    // https://www.charlezz.com/?p=189
    // https://soda1127.github.io/start-rx-kotlin/
    // https://tourspace.tistory.com/281?category=797357 마지막에 보던 것
    // ObservableAndConnectableObservable().run()
    // Subject().run()

//    println("Initial Out put with a = 15, b = 10")
//    val calculator = ReactiveCalculator(15, 10)
//
//    println("Enter a = <number> or b = <number> in separate lines\nexit to exit the program")
//    var line:String?
//    do {
//        line = readLine();
//        runBlocking {
//            async {
//                calculator.handleInput(line)
//            }
//        }
//    } while (line!= null && !line.toLowerCase().contains("exit"))

//    val time = async(CommonPoll) { longRunningTask() }
//    println("Print after async")
//    runBlocking { println("printing time ${time.await()}") }

//    runBlocking {
//        val exeTime = longRunningTask()
//        println("Execution Time is $exeTime")
//    }

//    val ct = CoroutineTest()
//    ct.fibonacciFun()
    //ct.sumAll()
    //ct.yieldExample()
//    ct.log("main() started.")
//    ct.launchInGlobalScope()
//    ct.log("launchInGlobalScope() executed")
//    Thread.sleep(5000L)
//    ct.log("main() terminated.")

//    println(createTable())
//
//    val i1 = Issue("IDEA-154446", "IDEA", "Bug", "Major", "Save setting faild")
//    val i2 = Issue("KT-12183", "Kotlin", "Feature", "Normal", "Intention: convert several calls on the same receiver to with/apple")
//    val predicate = ImportantIssuesPredicate("IDEA")
//    listOf(i1, i2).filter(predicate).forEach {
//        println(it.id)
//    }
//    for(issue in listOf(i1, i2).filter(predicate)) {
//        println(issue.id)
//    }
//
//    Reactive().reactive()
//        Subject().run()
//    FlowableAndBackPressure().run()
//    RxKotlinOperator().run()
//    Scheduler().run()
//    Using().run()
    CustomOperator().run()
}


fun createTable() =
    table {
        tr {
            td {
            }
        }
    }

data class Issue(val id: String ,val project: String, val type: String, val priority: String, val description: String)

class ImportantIssuesPredicate(val project: String):(Issue) -> Boolean {
    override fun invoke(p1: Issue): Boolean {
        return p1.project == project && p1.isImportant()
    }

    private fun Issue.isImportant(): Boolean {
        return type == "Bug" && (priority == "Major" || priority == "Critical")
    }
}

open class Tag(val name: String) {
    private val children = mutableListOf<Tag>()
    protected fun <T: Tag> doInit(child: T, init: T.() -> Unit) {
        child.init()
        children.add(child)
    }

    override fun toString(): String = "<$name>${children.joinToString("")}</$name>"
}

fun table(init: TABLE.() -> Unit) = TABLE().apply(init)

class TABLE: Tag("table") {
    fun tr(init: TR.() -> Unit) = doInit(TR(), init)
}

class TR: Tag("tr"){
    fun td(init: TD.() -> Unit) = doInit(TD(), init)
}

class TD: Tag("td")


suspend fun longRunningTask(): Long {
    val time = measureTimeMillis {
        println("Please wait")
        delay(2000)
        println("Delay Over")
    }
    return time
}

