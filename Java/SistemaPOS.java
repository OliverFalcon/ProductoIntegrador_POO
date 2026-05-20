import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// 1. Excepción Personalizada
class ProductoNoEncontradoException extends Exception {
    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

// 2. Entidad Base (Abstracción)
abstract class EntidadBase {
    protected String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

// 3. Entidad Concreta
class Producto extends EntidadBase {
    private String nombre;
    private double precio;
    private int stock;

    public Producto(String id, String nombre, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nombre + " | $" + precio + " | Stock: " + stock;
    }
}

// 4. Repositorio Genérico (Manejo de Colecciones Dinámicas)
class Repositorio<T extends EntidadBase> {
    private final List<T> elementos;

    public Repositorio() {
        this.elementos = new ArrayList<>();
    }

    public void agregar(T elemento) {
        elementos.add(elemento);
    }

    public List<T> obtenerTodos() {
        return new ArrayList<>(elementos);
    }

    public Optional<T> obtenerPorId(String id) {
        return elementos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public boolean eliminarPorId(String id) throws ProductoNoEncontradoException {
        T elemento = obtenerPorId(id).orElseThrow(() -> 
            new ProductoNoEncontradoException("Error: El ID " + id + " no existe."));
        return elementos.remove(elemento);
    }
}

// 5. Clase Principal
public class SistemaPOS {
    public static void main(String[] args) {
        Repositorio<Producto> inventario = new Repositorio<>();
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        // Carga inicial sugerida en el documento
        inventario.agregar(new Producto("001", "Arroz 1kg", 35.0, 10));
        inventario.agregar(new Producto("002", "Azúcar 1kg", 25.0, 10));

        while (opcion != 4) {
            System.out.println("\n--- SISTEMA POS E INVENTARIO ---");
            System.out.println("1. Mostrar Catálogo");
            System.out.println("2. Agregar Producto");
            System.out.println("3. Eliminar Producto");
            System.out.println("4. Salir");
            System.out.print("Elige una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (opcion) {
                    case 1:
                        System.out.println("\n--- INVENTARIO ACTUAL ---");
                        for (Producto p : inventario.obtenerTodos()) {
                            System.out.println(p.toString());
                        }
                        break;
                    case 2:
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Nombre: "); String nombre = scanner.nextLine();
                        System.out.print("Precio: "); double precio = scanner.nextDouble();
                        System.out.print("Stock: "); int stock = scanner.nextInt();
                        inventario.agregar(new Producto(id, nombre, precio, stock));
                        System.out.println("¡Agregado exitosamente!");
                        break;
                    case 3:
                        System.out.print("Introduce el ID a eliminar: ");
                        String idEliminar = scanner.nextLine();
                        inventario.eliminarPorId(idEliminar);
                        System.out.println("Producto eliminado.");
                        break;
                }
            } catch (ProductoNoEncontradoException e) {
                System.out.println(e.getMessage());
            }
        }
        scanner.close();
    }
}
