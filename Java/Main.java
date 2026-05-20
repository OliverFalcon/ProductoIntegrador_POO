import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// ==========================================
// 1. EXCEPCIONES PERSONALIZADAS
// ==========================================
class ProductoNoEncontradoException extends Exception {
    public ProductoNoEncontradoException(String mensaje) { super(mensaje); }
}

class StockInsuficienteException extends Exception {
    public StockInsuficienteException(String mensaje) { super(mensaje); }
}

// ==========================================
// 2. CLASE ABSTRACTA PARA MENÚS (Requisito DOCX)
// ==========================================
abstract class ComponenteMenu {
    protected static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static String leer(String texto) {
        System.out.print(texto);
        try {
            String entrada = reader.readLine();
            return (entrada != null && !entrada.trim().isEmpty()) ? entrada.trim() : null;
        } catch (IOException e) {
            return null;
        }
    }
}

// ==========================================
// 3. ENTIDADES BASE (Identidad de Objetos)
// ==========================================
abstract class EntidadBase {
    protected String id;
    public EntidadBase(String id) { this.id = id; }
    public String getId() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadBase that = (EntidadBase) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
    public abstract String aLineaCSV();
}

class Producto extends EntidadBase {
    private String nombre;
    private double precio;
    private int stock;

    public Producto(String id, String nombre, double precio, int stock) {
        super(id);
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String aLineaCSV() { return id + "," + nombre + "," + precio + "," + stock; }

    @Override
    public String toString() {
        return String.format("%-5s %-25s %-10.2f %-10d", id, nombre, precio, stock);
    }
}

class Venta extends EntidadBase {
    private String fecha;
    private double total;

    public Venta(String id, String fecha, double total) {
        super(id);
        this.fecha = fecha;
        this.total = total;
    }

    @Override
    public String aLineaCSV() { return id + "," + fecha + "," + total; }

    @Override
    public String toString() {
        return String.format("Ticket: %-5s | Fecha: %-12s | Total: $%.2f", id, fecha, total);
    }
}

// ==========================================
// 4. REPOSITORIO GENÉRICO (Gestión Dinámica)
// ==========================================
class Repositorio<T extends EntidadBase> {
    private final List<T> elementos = new ArrayList<>();

    public void agregar(T elemento) { elementos.add(elemento); }
    public List<T> obtenerTodos() { return Collections.unmodifiableList(elementos); }
    public Optional<T> obtenerPorId(String id) {
        return elementos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }
    public boolean actualizar(T objeto) {
        for (int i = 0; i < elementos.size(); i++) {
            if (elementos.get(i).getId().equals(objeto.getId())) {
                elementos.set(i, objeto);
                return true;
            }
        }
        return false;
    }
    public int contar() { return elementos.size(); }
}

// ==========================================
// 5. MANEJADOR DE ARCHIVOS (Persistencia)
// ==========================================
class ManejadorArchivos {
    public static void guardarProductos(Repositorio<Producto> repo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("productos.csv"))) {
            for (Producto p : repo.obtenerTodos()) pw.println(p.aLineaCSV());
        } catch (IOException e) { System.out.println("Error al guardar productos."); }
    }

    public static void cargarProductos(Repositorio<Producto> repo) {
        File archivo = new File("productos.csv");
        if (!archivo.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                if (d.length == 4) repo.agregar(new Producto(d[0], d[1], Double.parseDouble(d[2]), Integer.parseInt(d[3])));
            }
        } catch (Exception e) { System.out.println("Error al cargar productos."); }
    }

    public static void guardarVentas(Repositorio<Venta> repo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("ventas.csv"))) {
            for (Venta v : repo.obtenerTodos()) pw.println(v.aLineaCSV());
        } catch (IOException e) { System.out.println("Error al guardar ventas."); }
    }
}

// ==========================================
// 6. PROGRAMA PRINCIPAL Y MENÚS
// ==========================================
public class Main extends ComponenteMenu {
    private static Repositorio<Producto> repoProductos = new Repositorio<>();
    private static Repositorio<Venta> repoVentas = new Repositorio<>();
    private static int contadorTickets = 1;

    public static void main(String[] args) {
        ManejadorArchivos.cargarProductos(repoProductos);
        cargarDatosBaseSiVacio(); // Carga los 10 productos del Anexo original

        String opcion = "";
        do {
            System.out.println("\nMenu de Punto de Tienda de Abarrotes la Pequeña\n");
            System.out.println("1.-Productos ");
            System.out.println("2.-Punto de Venta ");
            System.out.println("3.-Inventario");
            System.out.println("4.-Ventas");
            System.out.println("5.-Salida \n");
            opcion = leer("Que opcion deseas    : ");

            if (opcion != null) {
                switch (opcion) {
                    case "1": menuProductos(); break;
                    case "2": menuPuntoVenta(); break;
                    case "3": menuInventario(); break;
                    case "4": listarVentas(); break;
                    case "5":
                        ManejadorArchivos.guardarProductos(repoProductos);
                        ManejadorArchivos.guardarVentas(repoVentas);
                        System.out.println("Salida del Sistema y datos guardados.");
                        break;
                    default: System.out.println("Opcion incorrecta");
                }
            }
        } while (!"5".equals(opcion));
    }

    // --- MÓDULO 1: PRODUCTOS ---
    private static void menuProductos() {
        String op = "";
        do {
            System.out.println("\nOpciones de Productos\n");
            System.out.println("1.-Modificar ");
            System.out.println("2.-Listado ");
            System.out.println("3.-Salida \n");
            op = leer("Que opcion deseas    : ");

            if ("1".equals(op)) modificarProducto();
            else if ("2".equals(op)) listarProductos();
        } while (!"3".equals(op));
    }

    private static void modificarProducto() {
        listarProductos();
        String id = leer("\nIntroduce el codigo del producto a modificar : ");
        try {
            Producto p = repoProductos.obtenerPorId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("no existe el codigo"));
            
            String precioStr = leer("Introduce el precio de " + p.getId() + "   " + p.getNombre() + " : ");
            if (precioStr != null) {
                p.setPrecio(Double.parseDouble(precioStr));
                repoProductos.actualizar(p);
                System.out.println("Precio actualizado.");
            }
        } catch (ProductoNoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("no es un valor numerico");
        }
    }

    private static void listarProductos() {
        System.out.println("");
        for (Producto p : repoProductos.obtenerTodos()) {
            System.out.println(p.toString());
        }
    }

    // --- MÓDULO 2: PUNTO DE VENTA ---
    private static void menuPuntoVenta() {
        List<Producto> carrito = new ArrayList<>();
        String ticketId = String.format("%03d", contadorTickets++);
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String op = "";

        do {
            System.out.println("\nFecha del Dia " + fecha + " Ticket No " + ticketId);
            System.out.println("-----------------------------------------------------");
            double subtotal = 0;
            for (Producto item : carrito) {
                System.out.println(item.getId() + "  " + item.getNombre() + "  $" + item.getPrecio());
                subtotal += item.getPrecio();
            }
            System.out.println("\n Menu de Punto de Venta\n");
            System.out.println("1.-Agregar  \n2.-Eliminar \n3.-Listado \n4.-Pagar \n5.-Salida \n");
            op = leer("Que opcion deseas : ");

            if ("1".equals(op)) {
                listarProductos();
                String idStr = leer("Introduce el codigo del producto: ");
                try {
                    Producto p = repoProductos.obtenerPorId(idStr)
                        .orElseThrow(() -> new ProductoNoEncontradoException("el codigo no existe no se puede agregar"));
                    if (p.getStock() <= 0) throw new StockInsuficienteException("no hay productos para venta");
                    
                    p.setStock(p.getStock() - 1);
                    carrito.add(new Producto(p.getId(), p.getNombre(), p.getPrecio(), 1)); // Clon al carrito
                    repoProductos.actualizar(p);
                } catch (Exception e) { System.out.println(e.getMessage()); }
            } 
            else if ("4".equals(op)) {
                double iva = subtotal * 0.16;
                double total = subtotal + iva;
                System.out.println("\n El total sin iva " + String.format("%.2f", subtotal));
                System.out.println(" el iva total es " + String.format("%.2f", iva));
                System.out.println(" el total de la venta fue " + String.format("%.2f", total));
                
                repoVentas.agregar(new Venta(ticketId, fecha, total));
                System.out.println("Venta pagada y registrada.");
                op = "5"; // Sale forzadamente al pagar
            }
            else if ("5".equals(op) && !carrito.isEmpty()) {
                System.out.println("No pago el ticket. Devolviendo productos al stock...");
                for (Producto item : carrito) {
                    Producto pReal = repoProductos.obtenerPorId(item.getId()).get();
                    pReal.setStock(pReal.getStock() + 1);
                    repoProductos.actualizar(pReal);
                }
            }
        } while (!"5".equals(op));
    }

    // --- MÓDULO 3: INVENTARIO ---
    private static void menuInventario() {
        String op = "";
        do {
            System.out.println("\nOpciones de Inventarios\n");
            System.out.println("1.-Listado \n2.-Agregar \n3.-Salida \n");
            op = leer("Que opcion deseas    : ");

            if ("1".equals(op)) listarProductos();
            else if ("2".equals(op)) {
                listarProductos();
                String id = leer("\nIntroduce el codigo del producto a modificar : ");
                try {
                    Producto p = repoProductos.obtenerPorId(id)
                        .orElseThrow(() -> new ProductoNoEncontradoException("no existe el codigo"));
                    String cantStr = leer("\nIntroduce la Cantidad de Stock a Agregar : ");
                    p.setStock(p.getStock() + Integer.parseInt(cantStr));
                    repoProductos.actualizar(p);
                    System.out.println("Stock actualizado.");
                } catch (ProductoNoEncontradoException e) { System.out.println(e.getMessage());
                } catch (NumberFormatException e) { System.out.println("no es un valor numerico"); }
            }
        } while (!"3".equals(op));
    }

    // --- MÓDULO 4: VENTAS ---
    private static void listarVentas() {
        System.out.println("\n--- LISTADO DE VENTAS ---");
        if (repoVentas.contar() == 0) System.out.println("No hay ventas registradas.");
        for (Venta v : repoVentas.obtenerTodos()) System.out.println(v.toString());
    }

    // --- CARGA INICIAL ---
    private static void cargarDatosBaseSiVacio() {
        if (repoProductos.contar() == 0) {
            repoProductos.agregar(new Producto("001", "Arroz 1kg", 35, 10));
            repoProductos.agregar(new Producto("002", "Azúcar 1kg", 25, 10));
            repoProductos.agregar(new Producto("003", "Harina 1kg", 28, 10));
            repoProductos.agregar(new Producto("004", "Aceite 1L", 50, 10));
            repoProductos.agregar(new Producto("005", "Leche 1L", 35, 10));
            repoProductos.agregar(new Producto("006", "Huevos 12 unidades", 45, 10));
            repoProductos.agregar(new Producto("007", "Fideos 500g", 20, 10));
            repoProductos.agregar(new Producto("008", "Sal 1kg", 15, 10));
            repoProductos.agregar(new Producto("009", "Pasta de tomate 400g", 25, 10));
            repoProductos.agregar(new Producto("010", "Atún lata 170g", 35, 10));
        }
    }
}
