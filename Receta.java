package GestorRecetas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Receta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String categoria;
    private int tiempoPreparacion; 
    private String dificultad;
    private ArrayList<String> ingredientes;

    public Receta() {
        this.ingredientes = new ArrayList<>();
    }

    public Receta(int id, String nombre, String categoria, int tiempoPreparacion,
                  String dificultad, List<String> ingredientes) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.ingredientes = new ArrayList<>(ingredientes);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(int tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public List<String> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<String> ingredientes) {
        this.ingredientes = new ArrayList<>(ingredientes);
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Receta: %-22s | Categoría: %-12s | Tiempo: %3d min | Dificultad: %-7s | Ingredientes: %s",
                id, nombre, categoria, tiempoPreparacion, dificultad, String.join(", ", ingredientes));
    }
}
