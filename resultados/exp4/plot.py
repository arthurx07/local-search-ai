import pandas as pd
import matplotlib.pyplot as plt

# Configuración global de matplotlib
plt.rcParams.update({
    'font.size': 12,           # tamaño general de texto
    'axes.titlesize': 14,      # tamaño de los títulos de los ejes
    'axes.labelsize': 12,      # tamaño de las etiquetas de los ejes
    'xtick.labelsize': 12,     # tamaño de los ticks X
    'ytick.labelsize': 12,     # tamaño de los ticks Y
    'legend.fontsize': 12,      # tamaño de la leyenda
    'lines.linewidth': 2,     # grosor de las líneas
    'lines.markersize': 8     # tamaño de los markers
})

# Cargar los datos desde el archivo CSV
df = pd.read_csv("runs.csv")

# Filtrar los datos por Hill Climbing (hc) y Simulated Annealing (sa)
df_hc = df[df['algoritmo'] == 'hc']
df_sa = df[df['algoritmo'] == 'sa']

# Calcular medias y desviaciones estándar para Hill Climbing
grouped_hc = df_hc.groupby(['grupos', 'centros']).agg({
    'tiempo_ms': ['mean', 'std'],
    'coste': ['mean', 'std']
}).reset_index()

# Calcular medias y desviaciones estándar para Simulated Annealing
grouped_sa = df_sa.groupby(['grupos', 'centros']).agg({
    'tiempo_ms': ['mean', 'std'],
    'coste': ['mean', 'std']
}).reset_index()

# Renombrar columnas para facilitar la referencia
grouped_hc.columns = ['grupos', 'centros', 'tiempo_hc_mean', 'tiempo_hc_std', 'coste_hc_mean', 'coste_hc_std']
grouped_sa.columns = ['grupos', 'centros', 'tiempo_sa_mean', 'tiempo_sa_std', 'coste_sa_mean', 'coste_sa_std']

# Crear etiquetas para el eje X en formato "grupos:centros"
x_labels = [f"{g}:{c}" for g, c in zip(grouped_hc['grupos'], grouped_hc['centros'])]
x_positions = range(len(x_labels))  # Posiciones en el eje X

# Gráfico de Coste
plt.figure(figsize=(10, 6))
plt.errorbar(x_positions, grouped_hc['coste_hc_mean'], yerr=grouped_hc['coste_hc_std'], 
             fmt='-o', label='Hill Climbing - Coste', capsize=5, color='blue', markersize=6)
plt.errorbar(x_positions, grouped_sa['coste_sa_mean'], yerr=grouped_sa['coste_sa_std'], 
             fmt='-o', label='Simulated Annealing - Coste', capsize=5, color='orange', markersize=6)
plt.title('Coste - Hill Climbing vs Simulated Annealing')
plt.xlabel('Grupos : Centros')
plt.ylabel('Coste')
plt.xticks(x_positions, x_labels)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("coste.png", dpi=300)

# Gráfico de Tiempo
plt.figure(figsize=(10, 6))
plt.errorbar(x_positions, grouped_hc['tiempo_hc_mean'] / 1e3, yerr=grouped_hc['tiempo_hc_std'] / 1e3, 
             fmt='-o', label='Hill Climbing - Tiempo', capsize=5, color='blue', markersize=6)
plt.errorbar(x_positions, grouped_sa['tiempo_sa_mean'] / 1e3, yerr=grouped_sa['tiempo_sa_std'] / 1e3, 
             fmt='-o', label='Simulated Annealing - Tiempo', capsize=5, color='orange', markersize=6)
plt.title('Tiempo - Hill Climbing vs Simulated Annealing')
plt.xlabel('Grupos : Centros')
plt.ylabel('Tiempo (segundos)')
plt.xticks(x_positions, x_labels)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("tiempo.png", dpi=300)

# Gráfico de Tiempo - Simulated Annealing (para más claridad)
plt.figure(figsize=(10, 6))
plt.errorbar(x_positions, grouped_sa['tiempo_sa_mean'], yerr=grouped_sa['tiempo_sa_std'], 
             fmt='-o', label='Simulated Annealing - Tiempo', capsize=5, color='orange', markersize=6)
plt.title('Tiempo - Simulated Annealing')
plt.xlabel('Grupos : Centros')
plt.ylabel('Tiempo (ms)')
plt.xticks(x_positions, x_labels)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("tiempo_sa.png", dpi=300)

###############################################################################
df2 = pd.read_csv("runs_sa.csv")
df2_sa = df2[df2['algoritmo'] == 'sa']

# Calcular medias y desviaciones estándar para Simulated Annealing
grouped2_sa = df2_sa.groupby(['grupos', 'centros']).agg({
    'tiempo_ms': ['mean', 'std'],
    'coste': ['mean', 'std']
}).reset_index()

# Renombrar columnas para facilitar la referencia
grouped2_sa.columns = ['grupos', 'centros', 'tiempo_sa_mean', 'tiempo_sa_std', 'coste_sa_mean', 'coste_sa_std']

# Crear etiquetas para el eje X en formato "grupos:centros"
x_labels2 = [f"{g}:{c}" for g, c in zip(grouped2_sa['grupos'], grouped2_sa['centros'])]
x_positions2 = range(len(x_labels2))  # Posiciones en el eje X

# Gráfico de Coste - Simulated Annealing (extendido)
plt.figure(figsize=(10, 6))
plt.errorbar(x_positions2, grouped2_sa['coste_sa_mean'], yerr=grouped2_sa['coste_sa_std'], 
             fmt='-o', label='Simulated Annealing - Coste', capsize=5, color='orange', markersize=6)
plt.title('Coste - Simulated Annealing (Extendido)')
plt.xlabel('Grupos : Centros')
plt.ylabel('Coste')
plt.xticks(x_positions2, x_labels2)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("coste_sa_extendido.png", dpi=300)

# Gráfico de Tiempo - Simulated Annealing (extendido)
plt.figure(figsize=(10, 6))
plt.errorbar(x_positions2, grouped2_sa['tiempo_sa_mean'], yerr=grouped2_sa['tiempo_sa_std'], 
             fmt='-o', label='Simulated Annealing - Tiempo', capsize=5, color='orange', markersize=6)
plt.title('Tiempo - Simulated Annealing (Extendido)')
plt.xlabel('Grupos : Centros')
plt.ylabel('Tiempo (ms)')
plt.xticks(x_positions2, x_labels2)
plt.grid(True)
plt.legend()
plt.tight_layout()
plt.savefig("tiempo_sa_extendido.png", dpi=300)
