# Búsqueda Local IA - FIB 2026 Q2

*Herman Daniel Berrio*  
*Artur Leivar*  
*Marc Pérez*  

## Instrucciones

Para correr el programa existen dos alternativas.  

La primera es:  
```
make jar
java -jar programa.jar
```

La segunda es:  

```
make run # ARGS="añadir aquí los argumentos"
```

A continuación se detalla como usar el programa.  

```
Uso:
  java -jar programa.jar [opciones]

Opciones:
  -g --grupos <n>                       Número de grupos (default 100)
  -c --centros <n>                      Número de centros (default 5)
  -l --helicopteros <n>                 Número de helicópteros (default 1)
  -s --semilla <n>                      Semilla aleatoria (default 1234)
  -i --inicial <greedy|aleatorio>       Generación del estado inicial (default greedy)
  -u --heuristica <1|2>                 Heurística a usar (default 1)
  -w --weight <n>                       Peso para la heurística 2, si se usa (default 5.0)
  -a --algoritmo <hc|sa>                Algoritmo de búsqueda (default hc)
  -o --operadores <swap|move|swap+move> Operadores (default swap+move)
  -z --opt3 <true|false>                Optimizador local de 3 grupos (default true)

Opciones específicas para Simulated Annealing:
  -t --steps <n>                        Número de iteraciones totales (default 2000)
  -e --stiter <n>                       Pasos por cada bajada de temp. (default 100)
  -k --k <n>                            Escala de temperatura (default 5)
  -d --lambda <n>                       Factor de enfriamiento (default 0.001)

  -b --debug                            Muestra información extra de la ejecución
  -h --help                             Muestra esta ayuda
```

# Información sobre la organización del repositorio
El directorio `src` incluye el código fuente del programa.
El directorio `resultados` contiene los resultados de realizar los experimentos sobre el programa, y las gráficas para poder analizarlos posteriormente.
El script `experimentos.sh` se ha escrito para automatizar el proceso de experimentación del programa.
