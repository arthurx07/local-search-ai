import pandas as pd
import matplotlib.pyplot as plt

# Configuración global de matplotlib
plt.rcParams.update({
    'font.size': 12,
    'axes.titlesize': 14,
    'axes.labelsize': 12,
    'xtick.labelsize': 12,
    'ytick.labelsize': 12,
    'legend.fontsize': 12,
    'lines.linewidth': 2,
    'lines.markersize': 8
})

# Cargar los datos
df = pd.read_csv("runs.csv")

# Algoritmos a analizar
algoritmos = ["hc", "sa"]

for alg in algoritmos:
    df_alg = df[df['algoritmo'] == alg]

    # Agrupar por peso del segundo criterio
    grouped = df_alg.groupby("peso_h2").agg({
        "tiempo_ms": ["mean", "std"],
        "coste": ["mean", "std"]
    }).reset_index()

    grouped.columns = ["peso_h2", "tiempo_mean", "tiempo_std", "coste_mean", "coste_std"]

    # Gráfico de Coste vs Peso del segundo criterio
    plt.figure(figsize=(8,5))
    plt.errorbar(grouped["peso_h2"], grouped["coste_mean"], yerr=grouped["coste_std"],
                 fmt='-o', capsize=5, label=f'{alg} - Coste')
    plt.title(f'Coste vs Peso del segundo criterio ({alg})')
    plt.xlabel('Peso del segundo criterio')
    plt.ylabel('Coste medio')
    plt.grid(True)
    plt.legend()
    plt.savefig(f"coste_{alg}.png", dpi=300)
    plt.close()

    # Gráfico de Tiempo vs Peso del segundo criterio
    plt.figure(figsize=(8,5))
    plt.errorbar(grouped["peso_h2"], grouped["tiempo_mean"], yerr=grouped["tiempo_std"],
                 fmt='-o', capsize=5, color='orange', label=f'{alg} - Tiempo')
    plt.title(f'Tiempo vs Peso del segundo criterio ({alg})')
    plt.xlabel('Peso del segundo criterio')
    plt.ylabel('Tiempo medio (ms)')
    plt.grid(True)
    plt.legend()
    plt.savefig(f"tiempo_{alg}.png", dpi=300)
    plt.close()

# Gráfico comparativo Coste y Tiempo para ambos algoritmos
for metric in ["coste", "tiempo_ms"]:
    plt.figure(figsize=(8,5))
    for alg, color in zip(algoritmos, ["blue", "orange"]):
        df_alg = df[df['algoritmo'] == alg]
        grouped = df_alg.groupby("peso_h2")[metric].mean().reset_index()
        plt.plot(grouped["peso_h2"], grouped[metric], '-o', label=alg, color=color)
    
    plt.title(f'{metric} vs Peso del segundo criterio (Comparativo)')
    plt.xlabel('Peso del segundo criterio')
    plt.ylabel('Costo medio' if metric=="coste" else 'Tiempo medio (ms)')
    plt.grid(True)
    plt.legend()
    plt.savefig(f"{metric}_comparativo.png", dpi=300)
    plt.close()
