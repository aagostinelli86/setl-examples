package com.github.joristruong.entry_point.lesson

import com.jcdecaux.setl.Setl
import com.jcdecaux.setl.config.ConfigLoader

/**
 * Learn about SETL entry point and different configurations
 */
object App {
  def main(args: Array[String]): Unit = {
    /**
     * This is the minimum code needed to create a `Setl` object. It is the entry point of every SETL app.
     * This will create a SparkSession, which is the entry point of any Spark job.
     * Additionally, the `withDefaultConfigLoader()` method is used. This means that `Setl` will read the default ConfigLoader located in `resources/application.conf`, where `setl.environment` must be set.
     * The ConfigLoader will then read the corresponding configuration file <app_env>.conf in the `resources` folder, where <app_env> is the value set for `setl.environment`.
     */
    println("Logs for: setl0")
    val setl0: Setl = Setl.builder()
      .withDefaultConfigLoader()
      .getOrCreate()

    /**
     * If you look at the `application.conf` file, `setl.environment` is set to use the system property defined by `app.environment`.
     * In `pom.xml`, the property is set at `local`. This can be overridden by setting the corresponding VM option.
     * You can specify your `SparkSession` options in the configuration file, under `setl.config.spark`.
     * As you can see in `local.conf`, I set `some.config.option` and you can see the value in the logs.
     */
    println(setl0.configLoader.appEnv)

    /**
     * You can also specify the configuration file that the `ConfigLoader` should read.
     */
    println("Logs for: setl1")
    val setl1: Setl = Setl.builder()
      .withDefaultConfigLoader("own_config_file.conf")
      .getOrCreate()

    /**
     * Or even set your own ConfigLoader.
     */
    println("Logs for: setl2")
    val configLoader: ConfigLoader = ConfigLoader.builder()
      .setAppEnv("local")
      .setAppName("Setl2_AppName")
      .setProperty("setl.config.spark.master", "local[*]")
      .setProperty("setl.config.spark.custom-key", "custom-value")
      .getOrCreate()
    val setl2: Setl = Setl.builder()
      .setConfigLoader(configLoader)
      .getOrCreate()

    /**
     * Some methods to quickly set your conf
     */
    println("Logs for: setl3")
    val setl3: Setl = Setl.builder()
      .withDefaultConfigLoader()
      .setSparkMaster("local[*]") // set your master URL
      .setShufflePartitions(200) // spark setShufflePartitions
      .getOrCreate()

    /**
     * Set the `SetlConfigPath`, where your conf options have to be found under it
     */
    println("Logs for: setl4")
    val setl4: Setl = Setl.builder()
      .withDefaultConfigLoader("own_config_file.conf")
      .setSetlConfigPath("myApp")
      .getOrCreate()

    /**
     * You can also set your own `SparkSession`. Please refer to the documentation, or the source code.
     * The simplest entry point and the most common should be setl0. Try to be familiar with it!
     */
  }
}
