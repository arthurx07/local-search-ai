import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("runs.csv")

# Agrupar por número de helicópteros
grouped = df.groupby("helicopteros").agg({
    "coste": ["mean", "std"],
    "tiempo_ms": ["mean", "std"]
}).reset_index()

grouped.columns = ["helicopteros", "coste_mean", "coste_std", "tiempo_mean", "tiempo_std"]

# Plot 1: Coste
plt.figure(figsize=(8,5))
plt.errorbar(grouped["helicopteros"], grouped["coste_mean"],
             yerr=grouped["coste_std"], marker='o', capsize=5)
plt.title("Coste vs. Número de Helicópteros")
plt.xlabel("Número de Helicópteros")
plt.ylabel("Coste medio")
plt.grid(True)

plt.savefig("coste.png", dpi=300)

# Plot 2: Tiempo
plt.figure(figsize=(8,5))
plt.errorbar(grouped["helicopteros"], grouped["tiempo_mean"],
             yerr=grouped["tiempo_std"], marker='o', color='orange', capsize=5)
plt.title("Tiempo vs Número de Helicópteros")
plt.xlabel("Número de Helicópteros")
plt.ylabel("Tiempo medio (ms)")
plt.grid(True)

plt.savefig("tiempo.png", dpi=300)

# Plot 3: Trade-off coste vs tiempo
plt.figure(figsize=(8,5))
plt.plot(grouped["tiempo_mean"], grouped["coste_mean"], marker='o')
for i, h in enumerate(grouped["helicopteros"]):
    plt.text(grouped["tiempo_mean"][i], grouped["coste_mean"][i], str(h))

plt.title("Trade-off: Coste vs Tiempo")
plt.xlabel("Tiempo medio (ms)")
plt.ylabel("Coste medio")
plt.grid(True)

plt.savefig("tradeoff.png", dpi=300)
