
### Escuela Colombiana de Ingeniería
### Oscar Andres Sanchez Porras
### Arquitecturas de Software - ARSW
## Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch


### Dependencias:
####   Lecturas:
*  [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)  (Hasta 'Ending Threads')
*  [Threads vs Processes]( http://cs-fundamentals.com/tech-interview/java/differences-between-thread-and-process-in-java.php)

### Descripción
  Este ejercicio contiene una introducción a la programación con hilos en Java, además de la aplicación a un caso concreto.
  

**Parte I - Introducción a Hilos en Java**

1. De acuerdo con lo revisado en las lecturas, complete las clases CountThread, para que las mismas definan el ciclo de vida de un hilo que imprima por pantalla los números entre A y B.
2. Complete el método __main__ de la clase CountMainThreads para que:
	1. Cree 3 hilos de tipo CountThread, asignándole al primero el intervalo [0..99], al segundo [99..199], y al tercero [200..299].
	2. Inicie los tres hilos con 'start()'.
	3. Ejecute y revise la salida por pantalla. 
	4. Cambie el incio con 'start()' por 'run()'. Cómo cambia la salida?, por qué?.

	Al reemplazar start() por run(), la ejecución deja de ser concurrente.
	Con start() los hilos se ejecutan en paralelo y la salida es no determinística, es decir sin seguir un orden especifico.
	Con run() el código se ejecuta de manera secuencial en el hilo principal, produciendo una salida ordenada.

	![](img/Codigo1.png)

	![](img/codigo2.png)

	![](img/prueba.png)

**Parte II - Ejercicio Black List Search**


Para un software de vigilancia automática de seguridad informática se está desarrollando un componente encargado de validar las direcciones IP en varios miles de listas negras (de host maliciosos) conocidas, y reportar aquellas que existan en al menos cinco de dichas listas. 

Dicho componente está diseñado de acuerdo con el siguiente diagrama, donde:

- HostBlackListsDataSourceFacade es una clase que ofrece una 'fachada' para realizar consultas en cualquiera de las N listas negras registradas (método 'isInBlacklistServer'), y que permite también hacer un reporte a una base de datos local de cuando una dirección IP se considera peligrosa. Esta clase NO ES MODIFICABLE, pero se sabe que es 'Thread-Safe'.

- HostBlackListsValidator es una clase que ofrece el método 'checkHost', el cual, a través de la clase 'HostBlackListDataSourceFacade', valida en cada una de las listas negras un host determinado. En dicho método está considerada la política de que al encontrarse un HOST en al menos cinco listas negras, el mismo será registrado como 'no confiable', o como 'confiable' en caso contrario. Adicionalmente, retornará la lista de los números de las 'listas negras' en donde se encontró registrado el HOST.

![](img/Model.png)

Al usarse el módulo, la evidencia de que se hizo el registro como 'confiable' o 'no confiable' se dá por lo mensajes de LOGs:

INFO: HOST 205.24.34.55 Reported as trustworthy

INFO: HOST 205.24.34.55 Reported as NOT trustworthy


Al programa de prueba provisto (Main), le toma sólo algunos segundos análizar y reportar la dirección provista (200.24.34.55), ya que la misma está registrada más de cinco veces en los primeros servidores, por lo que no requiere recorrerlos todos. Sin embargo, hacer la búsqueda en casos donde NO hay reportes, o donde los mismos están dispersos en las miles de listas negras, toma bastante tiempo.

Éste, como cualquier método de búsqueda, puede verse como un problema [vergonzosamente paralelo](https://en.wikipedia.org/wiki/Embarrassingly_parallel), ya que no existen dependencias entre una partición del problema y otra.

Para 'refactorizar' este código, y hacer que explote la capacidad multi-núcleo de la CPU del equipo, realice lo siguiente:

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. Agregue a dicha clase un método que permita 'preguntarle' a las instancias del mismo (los hilos) cuantas ocurrencias de servidores maliciosos ha encontrado o encontró.

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. Haga que dicha función espere hasta que los N hilos terminen de resolver su respectivo sub-problema, agregue las ocurrencias encontradas por cada hilo a la lista que retorna el método, y entonces calcule (sumando el total de ocurrencuas encontradas por cada hilo) si el número de ocurrencias es mayor o igual a _BLACK_LIST_ALARM_COUNT_. Si se da este caso, al final se DEBE reportar el host como confiable o no confiable, y mostrar el listado con los números de las listas negras respectivas. Para lograr este comportamiento de 'espera' revise el método [join](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html) del API de concurrencia de Java. Tenga también en cuenta:

	* Dentro del método checkHost Se debe mantener el LOG que informa, antes de retornar el resultado, el número de listas negras revisadas VS. el número de listas negras total (línea 60). Se debe garantizar que dicha información sea verídica bajo el nuevo esquema de procesamiento en paralelo planteado.

	* Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.


**Parte II.I Para discutir la próxima clase (NO para implementar aún)**

La estrategia de paralelismo antes implementada es ineficiente en ciertos casos, pues la búsqueda se sigue realizando aún cuando los N hilos (en su conjunto) ya hayan encontrado el número mínimo de ocurrencias requeridas para reportar al servidor como malicioso. Cómo se podría modificar la implementación para minimizar el número de consultas en estos casos?, qué elemento nuevo traería esto al problema?

¿Por qué la estrategia actual es ineficiente?

Porque aunque el sistema ya encontró 5 ocurrencias.
Todos los hilos siguen ejecutándose

Se consultan las 80.000 listas, se hacen llamadas innecesarias a isInBlackListServer

Esto es correcto funcionalmente, pero ineficiente en tiempo y recursos.

¿Cómo se podría mejorar la implementación?

La idea es detener la búsqueda cuando el total de ocurrencias encontradas por todos los hilos alcance el umbral.

¿Qué elemento NUEVO introduce esto al problema?
Sincronización entre hilos
Antes:

- Cada hilo era totalmente independiente

- El problema era paralelo

Ahora:

- Los hilos comparten estado

- Deben coordinarse

- Aparecen nuevos desafíos:

	- condiciones de carrera

	- visibilidad de memoria

	- consistencia de datos

**Evidencia**

![](img/codigo3.png)

![](img/codigo4.png)

![](img/codigo5.png)

![](img/prueba2.png)		

**Parte III - Evaluación de Desempeño**

A partir de lo anterior, implemente la siguiente secuencia de experimentos para realizar las validación de direcciones IP dispersas (por ejemplo 202.24.34.55), tomando los tiempos de ejecución de los mismos (asegúrese de hacerlos en la misma máquina):

![](img/parametrosIniciales.png)

1. Un solo hilo.
![](img/experimento1.png)

![](img/resultadoeExperimento1.png)

2. Tantos hilos como núcleos de procesamiento (haga que el programa determine esto haciendo uso del [API Runtime](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html)).
![](img/experimento2.png)

![](img/resultadoExperimento2.png)
3. Tantos hilos como el doble de núcleos de procesamiento.
![](img/experimento3.png)

![](img/resultadoExperimento3.png)
4. 50 hilos.
![](img/experimento4.png)

 ![](img/resultadoExperimento4.png)
5. 100 hilos.

 ![](img/Experimento5.png)

 ![](img/resultadoExperimento5.png)

Al iniciar el programa ejecute el monitor jVisualVM, y a medida que corran las pruebas, revise y anote el consumo de CPU y de memoria en cada caso. ![](img/jvisualvm.png)

Con lo anterior, y con los tiempos de ejecución dados, haga una gráfica de tiempo de solución vs. número de hilos. Analice y plantee hipótesis con su compañero para las siguientes preguntas (puede tener en cuenta lo reportado por jVisualVM):

El mejor desempeño es cuando el número de hilos es cercano al número de núcleos del procesador. Aumentar el número de hilos más allá de este punto genera sobrecarga por planificación y sincronización, incrementando el tiempo total de ejecución y el consumo de recursos.

En esta caso casi no se ve reflejado debido a que se implemento que los hilos se terminen cuando encuentren 5 ocurrencias. Es decir no se dejan ejecutando los hilos sino se detienen.

**Parte IV - Ejercicio Black List Search**

1. Según la [ley de Amdahls](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/#WhatisAmdahlsLaw?):

	![](img/ahmdahls.png)
	
	Donde _S(n)_ es el mejoramiento teórico del desempeño, _P_ la fracción paralelizable del algoritmo, y _n_ el número de hilos, a mayor _n_, mayor debería ser dicha mejora. Por qué el mejor desempeño no se logra con los 500 hilos?, cómo se compara este desempeño cuando se usan 200?. 
	Según la Ley de Amdahl, el speedup máximo que puede lograr un programa paralelo está limitado por la fracción del código que no puede paralelizarse. La ley se expresa como:

S(n) = 1 / ((1 − P) + P / n)

donde:
- S(n) es el speedup teórico,
- P es la fracción paralelizable del programa,
- n es el número de hilos o núcleos.

Aunque aumentar el numero de hilos reduce el tiempo de ejecucion de la parte paralela, la parte secuencial permanece constante y se convierte en cuello de botella. Por esta razon, agregar mas hilos produce rendimientos decrecientes.


En el experimento, al usar 500 hilos no se obtiene el mejor desempeño debido al aumento del overhead asociado a la creacion, gestion y sincronizacion de hilos, así como al incremento de cambios de contexto y contencion por recursos compartidos. En comparacion, usar 200 hilos ofrece un mejor balance entre paralelismo y sobrecarga, acercándose más al limite teorico de mejora descrito por la Ley de Amdahl.

Esto hace que más hilos no siempre indican un mejor rendimiento cuando existe una fraccion secuencial significativa.


2. Cómo se comporta la solución usando tantos hilos de procesamiento como núcleos comparado con el resultado de usar el doble de éste?.

Cuando se utilizan tantos hilos como núcleos disponibles, el sistema puede ejecutar cada hilo en un nucleo sin una competencia excesiva por los recursos del procesador. Esto suele producir el mejor rendimiento práctico.

Al usar el doble de hilos que nucleos, los hilos deben turnarse para ejecutarse, lo que introduce mayor overhead por planificación del sistema operativo y cambios de contexto. Como consecuencia, el beneficio del paralelismo disminuye y, en algunos casos, el tiempo total de ejecución puede incluso aumentar.

Este comportamiento concuerda con la Ley de Amdahl, ya que el beneficio marginal de agregar más hilos disminuye una vez que los recursos fisicos del sistema estan completamente utilizados.


3. De acuerdo con lo anterior, si para este problema en lugar de 100 hilos en una sola CPU se pudiera usar 1 hilo en cada una de 100 máquinas hipotéticas, la ley de Amdahls se aplicaría mejor?. Si en lugar de esto se usaran c hilos en 100/c máquinas distribuidas (siendo c es el número de núcleos de dichas máquinas), se mejoraría?. Explique su respuesta.

La Ley de Amdahl también se aplica a sistemas distribuidos. En este escenario, usar 1 hilo en cada una de 100 máquinas representa un paralelismo físico real, donde cada hilo se ejecuta en un procesador independiente sin competir por los mismos recursos locales.

Esto permitiría que la parte paralelizable del algoritmo se ejecute de forma más cercana al límite teorico de speedup, reduciendo los problemas de contención presentes en una sola CPU con muchos hilos.

Sin embargo, este enfoque introduce nuevos costos al problema, como la latencia de comunicación entre máquinas, la sincronización distribuida y la coordinacion de resultados. Estos factores pueden convertirse en nuevos cuellos de botella y limitar la mejora total del rendimiento.

Por lo tanto, aunque el desempeño podría mejorar frente a usar muchos hilos en una sola CPU, la ganancia final dependerá del costo de comunicación y coordinación entre los nodos distribuidos.



