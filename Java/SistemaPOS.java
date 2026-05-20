import java.io.*;
import java.util.*;

// ==========================================
// 1. EXCEPCIONES PERSONALIZADAS
// ==========================================
class ItemNoEncontradoException extends Exception {
    public ItemNoEncontradoException(String mensaje) { super(mensaje); }
}

class StockInsuficienteException extends Exception {
    public StockInsuficienteException(String mensaje) { super(mensaje); }
}

// ==========================================
// 2. CLASES DE MODELO (ENTIDADES)
// ==========================================
abstract class EntidadBase {
    protected String id;
    public EntidadBase(String id) { this.id = id; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Implementación obligatoria de equals y hashCode por ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadBase that = (EntidadBase) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Método abstracto para convertir a línea de texto (CSV) para guardar en .txt
    public abstract String aLineaCSV();
}

class Categoria extends EntidadBase {
    private String nombre;
    private String descripcion;

    public Categoria(String id, String nombre, String descripcion) {
        super(id);
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return id + " | " + nombre + " | " + descripcion;
    }

    @Override
    public String aLineaCSV() {
        return id + "," + nombre + "," + descripcion;
    }
}

class Producto extends EntidadBase {
    private String nombre;
    private String descripcion;
    private String idCategoria;
    private double precioCompra;
    private double precioVenta;
    private int stock;

    public Producto(String id, String nombre, String descripcion, String idCategoria, double precioCompra, double precioVenta, int stock) {
        super(id);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idCategoria = idCategoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIdCategoria() { return idCategoria; }
    public void setIdCategoria(String idCategoria) { this.idCategoria = idCategoria; }
    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return id + " | " + nombre + " | Stock: " + stock + " | Precio: $" + precioVenta;
    }

    @Override
    public String aLineaCSV() {
        return id + "," + nombre + "," + descripcion + "," + idCategoria + "," + precioCompra + "," + precioVenta + "," + stock;
    }
}
// ==========================================
// 3. REPOSITORIO GENÉRICO COMPLETO (CRUD)
// ==========================================
class Repositorio<T extends EntidadBase> {
    private final List<T> elementos;

    public Repositorio() {
        this.elementos = new ArrayList<>();
    }

    // --- MÉTODOS DE INSERCIÓN (CREATE) ---
    public void agregar(T elemento) {
        if (!existe(elemento.getId())) {
            elementos.add(elemento);
        }
    }

    // --- MÉTODOS DE LECTURA (READ/OBTENER) ---
    public List<T> obtenerTodos() {
        return new ArrayList<>(elementos); // Retorna copia inmutable para proteger datos
    }

    public Optional<T> obtenerPorIndice(int index) {
        if (index >= 0 && index < elementos.size()) {
            return Optional.of(elementos.get(index));
        }
        return Optional.empty();
    }

    public Optional<T> obtenerPorId(String id) {
        return elementos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    // --- MÉTODOS DE ACTUALIZACIÓN (UPDATE/MODIFICAR) ---
    public boolean modificar(String id, T nuevoObjeto) {
        for (int i = 0; i < elementos.size(); i++) {
            if (elementos.get(i).getId().equals(id)) {
                elementos.set(i, nuevoObjeto);
                return true;
            }
        }
        return false;
    }

    public boolean actualizar(T objeto) {
        return modificar(objeto.getId(), objeto);
    }

    // --- MÉTODOS DE ELIMINACIÓN (DELETE) ---
    public boolean eliminarPorId(String id) {
        return elementos.removeIf(e -> e.getId().equals(id));
    }

    public void limpiarTodo() {
        elementos.clear();
    }

    public boolean existe(String id) {
        return obtenerPorId(id).isPresent();
    }

    public int contar() {
        return elementos.size();
    }
}

// ==========================================
// 4. MANEJADOR DE ARCHIVOS (PERSISTENCIA TXT)
// ==========================================
class ManejadorArchivos {
    private static final String DIR = "datos/";

    public ManejadorArchivos() {
        File directorio = new File(DIR);
        if (!directorio.exists()) {
            directorio.mkdirs(); // Crea la carpeta "datos" si no existe
        }
    }

    // Guardar y Cargar Categorías
    public void guardarCategorias(List<Categoria> categorias) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "categorias.txt"))) {
            for (Categoria c : categorias) {
                pw.println(c.aLineaCSV());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar categorías: " + e.getMessage());
        }
    }

    public void cargarCategorias(Repositorio<Categoria> repo) {
        File archivo = new File(DIR + "categorias.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 3) {
                    repo.agregar(new Categoria(datos[0], datos[1], datos[2]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar categorías.");
        }
    }

    // Guardar y Cargar Productos
    public void guardarProductos(List<Producto> productos) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "productos.txt"))) {
            for (Producto p : productos) {
                pw.println(p.aLineaCSV());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar productos: " + e.getMessage());
        }
    }

    public void cargarProductos(Repositorio<Producto> repo) {
        File archivo = new File(DIR + "productos.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 7) {
                    repo.agregar(new Producto(
                        datos[0], datos[1], datos[2], datos[3],
                        Double.parseDouble(datos[4]), Double.parseDouble(datos[5]),
                        Integer.parseInt(datos[6])
                    ));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar productos.");
        }
    }
}
// ==========================================
// 5. CLASE PRINCIPAL (MENÚS Y LÓGICA)
// ==========================================
public class SistemaPOS {
    private static Repositorio<Categoria> repoCategorias = new Repositorio<>();
    private static Repositorio<Producto> repoProductos = new Repositorio<>();
    private static ManejadorArchivos manejador = new ManejadorArchivos();
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        // 1. Cargar datos desde los archivos .txt al iniciar
        manejador.cargarCategorias(repoCategorias);
        manejador.cargarProductos(repoProductos);

        // 2. Auto-generar datos si está vacío (Requisito de 10 Categorías y 500 Productos)
        generarDatosInicialesSiEstaVacio();

        int opcion = 0;
        while (opcion != 4) {
            System.out.println("\n=========================================");
            System.out.println("  SISTEMA POS - MENÚ PRINCIPAL");
            System.out.println("=========================================");
            System.out.println("1. Gestión de Categorías");
            System.out.println("2. Gestión de Productos");
            System.out.println("3. Punto de Venta (Simulación rápida)");
            System.out.println("4. Guardar y Salir");
            System.out.print("Elige una opción: ");

            try {
                opcion = Integer.parseInt(reader.readLine());

                switch (opcion) {
                    case 1: menuCategorias(); break;
                    case 2: menuProductos(); break;
                    case 3: menuVenta(); break;
                    case 4:
                        System.out.println("Guardando datos en archivos .txt...");
                        manejador.guardarCategorias(repoCategorias.obtenerTodos());
                        manejador.guardarProductos(repoProductos.obtenerTodos());
                        System.out.println("¡Datos guardados! Saliendo del sistema...");
                        break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error de entrada. Ingresa un número válido.");
            }
        }
    }

    // --- SUBMENÚ DE CATEGORÍAS ---
    private static void menuCategorias() throws IOException {
        System.out.println("\n--- LISTADO DE CATEGORÍAS (" + repoCategorias.contar() + ") ---");
        for (Categoria c : repoCategorias.obtenerTodos()) {
            System.out.println(c.toString());
        }
        System.out.println("-----------------------------------");
    }

    // --- SUBMENÚ DE PRODUCTOS ---
    private static void menuProductos() throws IOException {
        System.out.println("\n--- LISTADO DE PRODUCTOS (" + repoProductos.contar() + ") ---");
        // Para no saturar la consola con 500 productos, mostramos solo los primeros 10
        List<Producto> lista = repoProductos.obtenerTodos();
        int limite = Math.min(lista.size(), 10);
        for (int i = 0; i < limite; i++) {
            System.out.println(lista.get(i).toString());
        }
        if (lista.size() > 10) {
            System.out.println("... y " + (lista.size() - 10) + " productos más guardados en memoria.");
        }
        System.out.println("-----------------------------------");
    }

    // --- SUBMENÚ DE PUNTO DE VENTA ---
    private static void menuVenta() throws IOException {
        System.out.print("\nIngrese el ID del producto a vender: ");
        String idBuscar = reader.readLine();

        try {
            Producto p = repoProductos.obtenerPorId(idBuscar)
                .orElseThrow(() -> new ItemNoEncontradoException("Error: El producto con ID " + idBuscar + " no existe."));
            
            if (p.getStock() <= 0) {
                throw new StockInsuficienteException("Error: No hay stock suficiente para " + p.getNombre());
            }

            System.out.println("¡Venta realizada! Producto: " + p.getNombre() + " | Cobro: $" + p.getPrecioVenta());
            p.setStock(p.getStock() - 1); // Disminuye el inventario
            repoProductos.actualizar(p);

        } catch (ItemNoEncontradoException | StockInsuficienteException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- MÉTODO PARA CUMPLIR REQUISITO DE 500 PRODUCTOS ---
    private static void generarDatosInicialesSiEstaVacio() {
        if (repoCategorias.contar() == 0 && repoProductos.contar() == 0) {
            System.out.println("[INFO] Archivos vacíos detectados. Generando 10 categorías y 500 productos (Catálogo Zorro Abarrotero)...");
            
            for (int i = 1; i <= 10; i++) {
                String idCat = String.format("%03d", i);
                repoCategorias.agregar(new Categoria(idCat, "Categoría " + i, "Descripción de categoría " + i));
            }

            for (int i = 1; i <= 500; i++) {
                String idProd = String.format("%03d", i);
                repoProductos.agregar(new Producto(
                    idProd, 
                    "Producto Abarrote " + i, 
                    "Desc Prod " + i, 
                    "001", // Se asigna a la primera categoría
                    10.0 + (i % 50), // Precio de compra simulado
                    15.0 + (i % 50), // Precio de venta simulado
                    100 // Stock inicial
                ));
            }
            // Guarda los archivos físicos inmediatamente
            manejador.guardarCategorias(repoCategorias.obtenerTodos());
            manejador.guardarProductos(repoProductos.obtenerTodos());
        }
    }
}
