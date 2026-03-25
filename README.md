# local-search-ai
***TODO: ACTUALIZAR ESTE README.***
FIB 2026 Q2 AI Local Search Project  

Para correr el programa existen dos alternativas.  

La primera es  
```
make jar
java -jar programa.jar
```

La segunda es  

```
make run # ARGS="añadir aquí los argumentos"
```

A continuación se detalla como usar el programa.  

```
Uso:
  java -jar programa.jar [opciones]

Opciones:
  -g --grupos <n>                 Número de grupos (default 100)
  -c --centros <n>                Número de centros (default 5)
  -h --helicopteros <n>           Número de helicópteros (default 1)
  -s --semilla <n>                Semilla aleatoria (default 1234)
  -e --estado <greedy|aleatorio>  Estado inicial (default greedy)
  -u --heuristica <1|2>           Heurística a usar (default 1)
  -a --algoritmo <hc|sa>          Algoritmo de búsqueda (default hc)
  -h --help                       Muestra esta ayuda
```

