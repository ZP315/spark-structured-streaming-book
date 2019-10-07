package pl.japila.spark

import org.apache.spark.sql.execution.streaming.LongOffset

/**
 * Demo: Stateful Aggregation with Watermark and Append Output Mode
 *
 * Used to demo the following:
 *
 * * <a href="https://jaceklaskowski.gitbooks.io/spark-structured-streaming/spark-sql-streaming-StateStoreSaveExec.html">StateStoreSaveExec Physical Operator</a> with Append output mode
 */
object StreamingAggregationAppendModeApp extends SparkStreamsApp {

  // FIXME Compare to FlatMapGroupsWithStateApp

  // FIXME Make it configurable from the command line
  spark.sparkContext.setLogLevel("OFF")

  // Define event "format"
  // Event time must be defined on a window or a timestamp
  import java.sql.Timestamp
  case class Event(time: Timestamp, value: Long, batch: Long)
  import scala.concurrent.duration._
  object Event {
    def apply(secs: Long, value: Long, batch: Long): Event = {
      Event(new Timestamp(secs.seconds.toMillis), value, batch)
    }
  }

  // Using memory data source for full control of the input
  import org.apache.spark.sql.execution.streaming.MemoryStream
  implicit val sqlCtx = spark.sqlContext
  import spark.implicits._
  val events = MemoryStream[Event]
  val sessions = events.toDS
  assert(sessions.isStreaming, "sessions must be a streaming Dataset")

  // FIXME Make it configurable from the command line
  import scala.concurrent.duration._
  val delayThreshold = 10.seconds
  val eventTime = "time"

  println(
    s"""
       |Setting watermark column and delay for EventTimeTimeout
       |... eventTime column: $eventTime
       |... delayThreshold:   $delayThreshold
       |""".stripMargin)
  val valuesWatermarked = sessions
    .withWatermark(eventTime, delayThreshold.toString)

  import org.apache.spark.sql.streaming.OutputMode

  // FIXME Configurable from the command line
  val queryOutputMode = OutputMode.Append

  import org.apache.spark.sql.functions._
  val windowDuration = 5.seconds
  import org.apache.spark.sql.functions.window
  val countsPer5secWindow = valuesWatermarked
    .groupBy(window(col(eventTime), windowDuration.toString) as "sliding_window")
    .agg(collect_list("batch") as "batches", collect_list("value") as "values")

  // FIXME Configurable from the command line
  deleteCheckpointLocation()

  import org.apache.spark.sql.streaming.OutputMode
  val streamingQuery = countsPer5secWindow
    .writeStream
    .format("memory")
    .queryName(queryName)
    .option("checkpointLocation", checkpointLocation)
    .outputMode(queryOutputMode)
    .start
  val currentStatus = streamingQuery.status.message
  val expectedStatus = "Waiting for data to arrive"
  assert(
    currentStatus == expectedStatus,
    s"""Current status: $currentStatus not $expectedStatus""")

  println(
    s"""
       |Demo: Stateful Aggregation with Watermark and Append Output Mode
     """.stripMargin)

  // Sorry, it's simply to copy and paste event sections
  // and track the batches :)
  // FIXME Create batch generator (to read data from a directory?)
  var batchNo: Int = 0

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(1,  1, batch = batchNo),
      Event(15, 2, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(1,  1, batch = batchNo),
      Event(15, 2, batch = batchNo),
      Event(35, 3, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(15,1, batch = batchNo),
      Event(15,2, batch = batchNo),
      Event(20,3, batch = batchNo),
      Event(26,4, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(36, 1, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

  {
    batchNo = batchNo + 1
    println(
      s"""
         |Batch $batchNo
      """.stripMargin)
    val batch = Seq(
      Event(50, 1, batch = batchNo))
    val currentOffset = events.addData(batch)
    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val currentWatermark = streamingQuery.lastProgress.eventTime.get("watermark")
    val currentWatermarkMs = toMillis(currentWatermark)
    println(s"Current watermark: $currentWatermarkMs ms")
    println()
    println(streamingQuery.lastProgress.prettyJson)

    spark
      .table(queryName)
      .orderBy("sliding_window")
      .show(truncate = false)
  }

  pause()

}