# HyperLogLog++

Реализация вероятностной структуры данных и алгоритма HyperLogLog++ посредством стандартной библиотеки Java/Kotlin + хеш-функция MurmurHash3 на Java

### Алгоритмы семейства LogLog:
Данные алгоритмы предназначенны для приблизительного подсчета уникальных елементов во множестве. Его погрешность составляет не более 2% от общего количества значений. Подробнее можно узнать [здесь](https://static.googleusercontent.com/media/research.google.com/ru//pubs/archive/40671.pdf)

### Пример использования:

##### 1 - Создание объекта:
```
var HyperLogLog = HyperLogLog(18)
```
Примечание: При создании объекта необходимо передать в него значение типа int, отвечающее за количество индексов в алгоритме. Значение должно быть не меньше 4, и не больше 18

##### 2 - Хеш
Необходимо захешировать исходное значение через хэш-функцию, которая равномерно размазывает области определения по области значений, иначе говоря равномерно распределяет значение битов в переменной. Можно воспользоваться хеш-функцией MurmurHash3, которая есть в сходниках:

```
var hashedValue: Long = MurmurHash3.hash64("example")
```

##### 3 - Добавление хеша
Затем добавить значение в структуру данных:
```
HyperLogLog.add(hashedValue)
```

##### 4 - Подсчет уникальных значений
После того, когда все нужные значения будут добавленны, можно подсчитать количество уникальных элементов:
```
HyperLogLog.estimateCount()
```

##### Дополнительно
Так же, в случае подсчета разными потоками, можно объеденить объекты для общего подсчета уникальных значений:
```
HyperLogLog.union(HyperLogLog2)
```

#### Простой пример чтения из файла:
```
import hash.MurmurHash3
import java.io.*


fun main(args: Array<String>) {
    var hash: Long;
    var hll = HyperLogLog(18);

    try {
        val file = File("C:\\Users\\user_name\\Downloads\\ip_addresses\\ip_addresses")
        val fr = FileReader(file)
        val reader = BufferedReader(fr)
        var line = reader.readLine()
        while (line != null) {
            hash = MurmurHash3.hash64(line.toByteArray())
            hll.add(hash)
            line = reader.readLine()
        }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    println(hll.estimateCount())
}
```

##### P.S
Данная реализация работает только с 64-х битными значениями. 

