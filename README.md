# local-search-ai
***TODO: ACTUALIZAR ESTE README.***  
FIB 2026 Q2 AI Local Search Project  

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
  -a --algoritmo <hc|sa>                Algoritmo de búsqueda (default hc)
  -o --operadores <swap|move|swap+move> Operadores (default swap+move)

Opciones específicas para Simulated Annealing:
  -t --steps <n>                        Número de iteraciones totales (default 2000)
  -k --k <n>                            Escala de temperatura (default 5)
  -l --lambda <n>                       Factor de enfriamiento (default 0.001)

  -h --help                             Muestra esta ayuda
```

