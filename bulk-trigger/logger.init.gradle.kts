import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

useLogger(CustomEventLogger())

class CustomEventLogger() : BuildAdapter(), TaskExecutionListener {

    private val startTimes = mutableMapOf<String, LocalDateTime>()

    override fun beforeExecute(task: Task) {
        startTimes[task.name] = now()
        println(" --> [${task.name}]")
    }
    override fun afterExecute(task: Task, state: TaskState) {
        println()
        val cmds = listOf("otel-cli", "span",
            "-n", "my-script",
            "-s", task.name,
            "--start", startTimes[task.name]!!.format(ISO_DATE_TIME),
            "--end", now().format(ISO_DATE_TIME))

        println(cmds.joinToString(" "))

        val process = ProcessBuilder(cmds)
            .start()

//        println(process);

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroy()
            throw RuntimeException("execution timed out: $this")
        }
        if (process.exitValue() != 0) {
            throw RuntimeException("execution failed with code ${process.exitValue()}: $this")
        }
    }

    override fun buildFinished(result: BuildResult) {
        println("build completed")
        if (result.failure != null) {
            (result.failure as Throwable).printStackTrace()
        }
    }
}
