# SETL examples

Lessons and exercises to get familiar with the wonderful [SETL](https://github.com/SETL-Developers/setl) framework!

# Chapters

## 1. Entry Point and configurations

<details> <summary><strong>Lesson</strong></summary>

<h3>1.1. Entry point with basic configurations</h3>

<details> <summary></summary>

The entry point is the first thing you need to learn to code with SETL. It is the starting point to run your ETL project.

```
val setl0: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()
```

This is the minimum code needed to create a `Setl` object. It is the entry point of every SETL app. This will create a SparkSession, which is the entry point of any Spark job. Additionally, the `withDefaultConfigLoader()` method is used. This means that `Setl` will read the default ConfigLoader located in `resources/application.conf`, where `setl.environment` must be set. The ConfigLoader will then read the corresponding configuration file `<app_env>.conf` in the `resources` folder, where `<app_env>` is the value set for `setl.environment`.

> `resources/application.conf`:
> ```
> setl.environment = <app.env>
> ```

> `<app.env>.conf`:
> ```
> setl.config.spark {
>    some.config.option = "some-value"
>  }
> ```

The configuration file is where you can specify your `SparkSession` options, like when you create one in a basic `Spark` process. You must specify your `SparkSession` options under `setl.config.spark`.

</details>

<h3>1.2. Entry point with specific configurations</h3>

<details> <summary></summary>

You can specify the configuration file that the default `ConfigLoader` should read. In the code below, instead of reading `<app_env>.conf` where `<app_env>` is defined in `application.conf`, it will read `own_config_file.conf`.
> ```
> val setl1: Setl = Setl.builder()
>     .withDefaultConfigLoader("own_config_file.conf")
>     .getOrCreate()
> ```
> 
> `resources/own_config_file.conf`:
> ```
> setl.config.spark {
>    some.config.option = "some-other-value"
>  }
> ```

You can also set your own `ConfigLoader`. In the code below, `Setl` will load `local.conf` from the `setAppEnv()` method. If no `<app_env>` is set, it will fetch the environment from the default `ConfigLoader`, located in `resources/application.conf`.
> ```
> val configLoader: ConfigLoader = ConfigLoader.builder()
>     .setAppEnv("local")
>     .setAppName("Setl2_AppName")
>     .setProperty("setl.config.spark.master", "local[*]")
>     .setProperty("setl.config.spark.custom-key", "custom-value")
>     .getOrCreate()
> val setl2: Setl = Setl.builder()
>     .setConfigLoader(configLoader)
>     .getOrCreate()
> ```
 
You can also set your own `SparkSession` which will be used by `Setl`, with the `setSparkSession()` method. Please refer to the documentation or the source code of [SETL](https://github.com/SETL-Developers/setl).

</details>

<h3>1.3 Utilities</h3>

<details> <summary></summary>

<h5>Helper methods</h5>

<details> <summary></summary>

There are some quick methods that can be used to set your `SparkSession` configurations.
> ```
> val setl3: Setl = Setl.builder()
>     .withDefaultConfigLoader()
>     .setSparkMaster("local[*]") // set your master URL
>     .setShufflePartitions(200) // spark setShufflePartitions
>     .getOrCreate()
> ```
 
* `setSparkMaster()` method set the `spark.master` property of the `SparkSession` in your `Setl` entry point
* `setShufflePartitions()` method set the `spark.sql.shuffle.partitions` property of the `SparkSession` in your `Setl` entry point

</details>

<h5>SparkSession options</h5>

<details> <summary></summary>

As mentioned earlier, the options you want to define in your `SparkSession` must be specified under `setl.config.spark` in your configuration file. However, you can change this path by using the `setlSetlConfigPath()` method:
> ```
> val setl4: Setl = Setl.builder()
>     .withDefaultConfigLoader("own_config_file.conf")
>     .setSetlConfigPath("myApp")
>     .getOrCreate()
> ```
> 
> `resources/own_config_file.conf`:
> ```
> myApp.spark {
>     some.config.option = "my-app-some-other-value"
> }
> ```

</details>

</details>

</details>

##

<details> <summary><strong>Exercises</strong></summary>

Nothing too crazy: try to build your own `Setl` object! Run your code and examine the logs to check about the options you specified. Make sure it loads the correct configuration file.

</details>

## 2. Extract

<details> <summary><strong>Lesson</strong></summary>

SETL supports two types of data accessors: Connector and SparkRepository.
* A Connector is a non-typed abstraction of data access layer (DAL). For simplicity, you can understand it to as a Spark DataFrame.
* A SparkRepository is a typed abstraction data access layer (DAL). For simplicity, you can understand it as a Spark Dataset.
For more information, please refer to the [official documentation](https://setl-developers.github.io/setl/).

`SETL` supports multiple data format, such as CSV, JSON, Parquet, Excel, Cassandra, DynamoDB, JDBC or Delta.

To ingest data in the `Setl` object entry point, you first must register the data, using the `setConnector()` or the `setSparkRepository[T]` methods.

### 2.1 Registration with `Connector`

<details> <summary></summary>

```
val setl: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl
    .setConnector("testObjectRepository", deliveryId = "id")
```

The first argument provided is a `String` that refers to an item in the specified configuration file. The second argument, `deliveryId`, must be specified for data ingestion. We will see in section **2.3** why it is necessary. Just think of it as an ID, and the only way for `SETL` to ingest a `Connector` is with its ID.

Note that `deliveryId` is not necessary for the registration but it is for the ingestion. However there is no much use if we only register the data. If you are a beginner in `SETL`, you should think as setting a `Connector` must always come with a `deliveryId`.

`local.conf`:
```
setl.config.spark {
  some.config.option = "some-value"
}

testObjectRepository {
  storage = "CSV"
  path = "src/main/resources/test_objects.csv"
  inferSchema = "true"
  delimiter = ","
  header = "true"
  saveMode = "Overwrite"
}
```

As you can see, `testObjectRepository` defines a configuration for data of type `CSV`. This data is in a file, located in `src/main/resources/test_objects.csv`. Other classic read or write options are configured.

In summary, to register a `Connector`, you need to:
1. Specify an item in your configuration file. This item must have a `storage` key, which represents the type of the data. Other keys might be mandatory depending on this type.
2. Register the data in your `Setl` object, using `setConnector("<item>", deliveryId = "<id>")`.

</details>

### 2.2 Registration with `SparkRepository`

<details> <summary></summary>

```
val setl: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl
    .setSparkRepository[TestObject]("testObjectRepository")
```

Like `setConnector()`, the argument provided is a `String` that refers to an item in the specified configuration file.

`local.conf`:
```
setl.config.spark {
  some.config.option = "some-value"
}

testObjectRepository {
  storage = "CSV"
  path = "src/main/resources/test_objects.csv"
  inferSchema = "true"
  delimiter = ","
  header = "true"
  saveMode = "Overwrite"
}
```

Notice that the above `SparkRepository` is set with the `TestObject` type. In this example, the data we want to register is a CSV file containing two columns: `value1` of type `String` and `value2` of type `Int`. That is why the `TestObject` class should be:
```
case class TestObject(value1: String,
                      value2: Int)
```

In summary, to register a `SparkRepository`, you need to:
1. Specify an item in your configuration file. This item must have a `storage` key, which represents the type of the data. Other keys might be mandatory depending on this type.
2. Create a class or a case class representing the object type of your data.
3. Register the data in your `Setl` object, using `setSparkRepository[T]("<item>")`.

</details>

<details> <summary></summary>
    
1. `Connector` or `SparkRepository`?

    Sometimes, the data your are ingesting contain irrelevant information that you do not want to keep. For example, let's say that the CSV file you want to ingest contain 10 columns: `value1`, `value2`, `value3` and 7 other columns you are not interested in.
    
    It is possible to ingest these 3 columns only with a `SparkRepository` if you specify the correct object type of your data:
    ```
    case class A(value1: T1,
                 value2: T2,
                 value3: T3)
    
    setl
        .setSparkRepository[A]("itemInConfFile")
    ```

    This is not possible with a `Connector`. If you register this CSV file with a `Connector`, all 10 columns will appear.

2. Annotations

* `@ColumnName`

    `@ColumnName` is an annotation used in a case class. When you want to rename some columns in your code for integrity but also keep the original name when writing the data, you can use this annotation.
    
    ```
    case class A(@ColumnName("value_one") valueOne: T1,
                 @ColumnName("value_two") valueTwo: T2)
    ```
  
  As you probably know, Scala does not use `snake_case` but `camelCase`. If you register a `SparkRepository` of type `[A]` in your `Setl` object, and if you read it, the columns will be named as `valueOne` and `valueTwo`. The file you read will still keep their name, i.e `value_one` and `value_two`.

* `@CompoundKey`

    TODO

* `@Compress`

    TODO

</details>

### 2.3 Registration of multiple data sources

Most of the time, you will need to register multiple data sources.

<details> <summary></summary>

#### 2.3.1 Multiple `Connector`

<details> <summary></summary>

Let's start with `Connector`. Note that it is perfectly possible to register multiple `Connector`, as said previously. However, there will be an issue during the ingestion. `Setl` has no way to differentiate one `Connector` from another. You will need to set what is called a `deliveryId`.

```
val setl1: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()
 
// /!\ This will work for data registration here but not for data ingestion later /!\
setl1
    .setConnector("testObjectRepository")
    .setConnector("pokeGradesRepository")
 
// Please get used to set a `deliveryId` when you register one or multiple `Connector`
setl1
    .setConnector("testObjectRepository", deliveryId = "testObject")
    .setConnector("pokeGradesRepository", deliveryId = "grades")
```

</details>

#### 2.3.2 Multiple `SparkRepository`

<details> <summary></summary>

Let's now look at how we can register multiple `SparRepository`. If the `SparkRepository` you register all have different type, there will be no issue during the ingestion. Indeed, `Setl` is capable of differentiating the upcoming data by inferring the object type.

```
val setl2: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl2
    .setSparkRepository[TestObject]("testObjectRepository")
    .setSparkRepository[Grade]("pokeGradesRepository")
```

However, if there are multiple `SparkRepository` with the same type, you **must** use a `deliveryId` for each of them. Otherwise, there will be an error during the data ingestion. This is the same reasoning as multiple `Connector`: there is no way to differentiate two `SparkRepository` of the same type.

```
val setl3: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

// /!\ This will work for data registration here but not for data ingestion later /!\
setl3
    .setSparkRepository[Grade]("pokeGradesRepository")
    .setSparkRepository[Grade]("digiGradesRepository")

// Please get used to set a `deliveryId` when you register multiple `SparkRepository` of same type
setl3
    .setSparkRepository[Grade]("pokeGradesRepository", deliveryId = "pokeGrades")
    .setSparkRepository[Grade]("digiGradesRepository", deliveryId = "digiGrades")
```

</details>

</details>

### 2.4 Data Ingestion

<details> <summary></summary>

Before deep diving into data ingestion, we first must learn about how `SETL` organizes an ETL process. `SETL` uses `Pipeline` and `Stage` to organize workflows. A `Pipeline` is where the whole ETL process will be done. The registered data are ingested inside a `Pipeline`, and all transformations and restitution will be done inside it. A `Pipeline` is composed of multiple `Stage`. A `Stage` allows you to modulate your project. It can be constituted of multiple `Factory`. You can understand a `Factory` as a module of your ETL process. So in order to "see" the data ingestion, we have to create a `Pipeline` and add a `Stage` to it. As it may be a little bit theoretical, let's look at some examples.

`App.scala`:
```
val setl4: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl4
    .setConnector("testObjectRepository", deliveryId = "testObjectConnector")
    .setSparkRepository[TestObject]("testObjectRepository", deliveryId = "testObjectRepository")

setl4
    .newPipeline() // Creation of a `Pipeline`.
    .addStage[IngestionFactory]() // Add a `Stage` composed of one `Factory`: `IngestionFactory`.
    .run()
```

Before running the code, let's take a look at `IngestionFactory`.

```
class IngestionFactory extends Factory[DataFrame] with HasSparkSession {

    import spark.implicits._

    override def read(): IngestionFactory.this.type = this

    override def process(): IngestionFactory.this.type = this

    override def write(): IngestionFactory.this.type = this

    override def get(): DataFrame = spark.emptyDataFrame
}
```

This is a skeleton of a `SETL Factory`. A `SETL Factory` contains 4 main functions: `read()`, `process()`, `write()` and `get()`. These functions will be executed in this order. These 4 functions are the core of your ETL process. This is where you will write your classic `Spark` code of data transformation.

You can see that `IngestionFactory` is a child class of `Factory[DataFrame]`. This simply means that the output of this data transformation must be a `DataFrame`. `IngestionFactory` also has the trait `HasSparkSession`. It allows you to access the `SparkSession` easily. Usually, we use it simply to import `spark.implicits`.

Where is the ingestion? 

```
class IngestionFactory extends Factory[DataFrame] with HasSparkSession {

    import spark.implicits._

    @Delivery(id = "testObjectConnector")
    val testObjectConnector: Connector = Connector.empty
    @Delivery(id = "testObjectRepository")
    val testObjectRepository: SparkRepository[TestObject] = SparkRepository[TestObject]
    
    var testObjectOne: DataFrame = spark.emptyDataFrame
    var testObjectTwo: Dataset[TestObject] = spark.emptyDataset[TestObject]

    override def read(): IngestionFactory.this.type = this

    override def process(): IngestionFactory.this.type = this

    override def write(): IngestionFactory.this.type = this

    override def get(): DataFrame = spark.emptyDataFrame
}
```

The structure of a `SETL Factory` starts with the `@Delivery` annotation. This annotation is the way `SETL` ingest the corresponding registered data. If you look at `App.scala` where this `IngestionFactory` is called, the associated `Setl` object has registered a `Connector` with id `testObjectConnector` and a `SparkRepository` with id `testObjectRepository`.

> Note that it is not mandatory to use a `deliveryId` in this case, because there is only one `Factory` with `TestObject` as object type. You can try to remove the `deliveryId` when registering the `SparkRepository` and the `id` in the `@Delivery` annotation. The code will still run. Same can be said for the `Connector`.

With the `@Delivery` annotation, we retrieved a `Connector` and `SparkRepository`. The data has been correctly ingested, but these are data access layers. To process the data, we have to retrieve the `DataFrame` of the `Connector` and the `Dataset` of the `SparkRepository`. This is why we defined two `var`, one of type `DataFrame` and one of type `Dataset[TestObject]`. We will assign values to them during the `read()` function. These `var` are accessible from all the 4 core functions, and you will use them for your ETL process.

To retrieve the `DataFrame` of the `Connector` and the `Dataset` of the `SparkRepository`, we can use the `read()` function.

```
override def read(): IngestionFactory.this.type = {
    testObjectOne = testObjectConnector.read()
    testObjectTwo = testObjectRepository.findAll()

    this
}
```

The `read()` function is typically where you will do your data preprocessing. Usually, we will simply assign values to our variables. Occasionally, this is typically where you would want to do some filtering on your data.

* To retrieve the `DataFrame` of a `Connector`, use the `read()` method.
* To retrieve the `Dataset` of a `SparkRepository`, you can use the `findAll()` method, or the `findBy()` method. The latter allows you to do filtering based on `Condition`. More info [here](https://setl-developers.github.io/setl/Condition).

The registered data is then correctly ingested. It is now ready to be used during the `process()` function.

</details>

### 2.5 Additional resources

<details> <summary></summary>

#### 2.5.1 `AutoLoad`

<details> <summary></summary>

In the previous `IngestionFactory`, we would set a `val` of type `SparkRepository` but also a `var` in which we assign the corresponding `Dataset` in the `read()` function. With `autoLoad = true`, we can skip the first step and directly declare a `Dataset`. The `Dataset` of the `SparkRepository` will be automatically assigned in it.

`App.scala`:
```
val setl5: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl5
    .setSparkRepository[TestObject]("testObjectRepository", deliveryId = "testObjectRepository")

setl5
    .newPipeline()
    .addStage[AutoLoadIngestionFactory]()
    .run()
```

`AutoLoadIngestionFactory`
```
class AutoLoadIngestionFactory extends Factory[DataFrame] with HasSparkSession {

  import spark.implicits._

  @Delivery(id = "testObjectRepository", autoLoad = true)
  val testObject: Dataset[TestObject] = spark.emptyDataset[TestObject]

  override def read(): AutoLoadIngestionFactory.this.type = {
    testObject.show(false)

    this
  }

  override def process(): AutoLoadIngestionFactory.this.type = this

  override def write(): AutoLoadIngestionFactory.this.type = this

  override def get(): DataFrame = spark.emptyDataFrame
}
```

Note that there is no way to use the `findBy()` method to filter the data, compared to the previous `Factory`. Also, `autoLoad` is available for `SparkRepository` only, and not for `Connector`.

</details>

#### 2.5.2 Adding parameters to the `Pipeline`

<details> <summary></summary>

If you want to set some primary type parameters, you can use the `setInput[T]()` method. Those *inputs* are directly set in the `Pipeline`, and there are no registrations like for `Connector` or `SparkRepository`.

`App.scala`:
```
val setl5: Setl = Setl.builder()
    .withDefaultConfigLoader()
    .getOrCreate()

setl5
    .newPipeline()
    .setInput[Int](42)
    .setInput[String]("SETL", deliveryId = "ordered")
    .setInput[String]("LTES", deliveryId = "reversed")
    .setInput[Array[String]](Array("S", "E", "T", "L"))
    .addStage[AutoLoadIngestionFactory]()
    .run()
```

*Inputs* are retrieved in the same way `Connector` or `SparkRepository` are retrieved: the `@Delivery` annotation, and the `deliveryId` if necessary.

`AutoLoadIngestionFactory.scala`:
```
class AutoLoadIngestionFactory extends Factory[DataFrame] with HasSparkSession {

  import spark.implicits._

  @Delivery
  val integer: Int = 0
  @Delivery(id = "ordered")
  val firstString: String = ""
  @Delivery(id = "reversed")
  val secondString: String = ""
  @Delivery
  val stringArray: Array[String] = Array()

  override def read(): AutoLoadIngestionFactory.this.type = {
    // Showing that inputs work correctly
    println("integer: " + integer) // integer: 42
    println("ordered: " + firstString) // ordered: SETL
    println("reversed: " + secondString) // reversed: LTES
    println("array: " + stringArray.mkString(".")) // array: S.E.T.L

    this
  }

  override def process(): AutoLoadIngestionFactory.this.type = this

  override def write(): AutoLoadIngestionFactory.this.type = this

  override def get(): DataFrame = spark.emptyDataFrame
}
```

</details>

</details>

### 2.6 Summary

In summary, the *extraction* part of an ETL process translates to the following in a `SETL` project:
1. Create a configuration item representing the data you want to ingest in your configuration file.
2. Register the data in your `Setl` object by using the `setConnector()` or the `setSparkRepository[]()` method. Reminder: the mandatory parameter is the name of your object in your configuration file, and you might want to add a `deliveryId`.
3. Create a new `Pipeline` in your `Setl` object, then add a `Stage` with a `Factory` in which you want to process your data.
4. Create a `SETL Factory`, containing the 4 core functions: `read()`, `process()`, `write()` and `get()`.
5. Retrieve your data using the `@Delivery` annotation.
6. Your data is ready to be processed. 

### 2.7 Data format configuration cheat sheet

Cheat sheet can be found [here](https://setl-developers.github.io/setl/data_access_layer/configuration_example).

</details>

##

<details> <summary><strong>Exercises</strong></summary>



</details>

## 3. Transform

<details> <summary><strong>Lesson</strong></summary>



</details>

##

<details> <summary><strong>Exercises</strong></summary>



</details>

## 4. Load

<details> <summary><strong>Lesson</strong></summary>



</details>

##

<details> <summary><strong>Exercises</strong></summary>



</details>

## 5. From local to production environment

<details> <summary><strong>Lesson</strong></summary>



</details>

##

<details> <summary><strong>Exercises</strong></summary>



</details>