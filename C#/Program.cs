using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

// 1. EXCEPCIONES PERSONALIZADAS
public class ProductoNoEncontradoException : Exception {
    public ProductoNoEncontradoException(string mensaje) : base(mensaje) { }
}
public class StockInsuficienteException : Exception {
    public StockInsuficienteException(string mensaje) : base(mensaje) { }
}

// 2. ENTIDADES BASE
public abstract class EntidadBase {
    public string Id { get; set; }
    protected EntidadBase(string id) { Id = id; }
    
    public override bool Equals(object obj) {
        if (obj is EntidadBase otra) return Id == otra.Id;
        return false;
    }
    public override int GetHashCode() { return Id.GetHashCode(); }
    public abstract string ALineaCSV();
}

public class Producto : EntidadBase {
    public string Nombre { get; set; }
    public double Precio { get; set; }
    public int Stock { get; set; }

    public Producto(string id, string nombre, double precio, int stock) : base(id) {
        Nombre = nombre; Precio = precio; Stock = stock;
    }
    public override string ALineaCSV() { return $"{Id},{Nombre},{Precio},{Stock}"; }
    public override string ToString() { return $"{Id,-5} {Nombre,-25} ${Precio,-10:F2} {Stock,-10}"; }
}

public class Venta : EntidadBase {
    public string Fecha { get; set; }
    public double Total { get; set; }

    public Venta(string id, string fecha, double total) : base(id) {
        Fecha = fecha; Total = total;
    }
    public override string ALineaCSV() { return $"{Id},{Fecha},{Total}"; }
    public override string ToString() { return $"Ticket: {Id,-5} | Fecha: {Fecha,-12} | Total: ${Total:F2}"; }
}

// 3. REPOSITORIO GENÉRICO
public class Repositorio<T> where T : EntidadBase {
    private readonly List<T> elementos = new List<T>();

    public void Agregar(T elemento) { elementos.Add(elemento); }
    public List<T> ObtenerTodos() { return elementos.ToList(); }
    public T ObtenerPorId(string id) { return elementos.FirstOrDefault(e => e.Id == id); }
    public bool Actualizar(T objeto) {
        var index = elementos.FindIndex(e => e.Id == objeto.Id);
        if (index >= 0) { elementos[index] = objeto; return true; }
        return false;
    }
    public int Contar() { return elementos.Count; }
}

// 4. PROGRAMA PRINCIPAL
class Program {
    static Repositorio<Producto> repoProductos = new Repositorio<Producto>();
    static Repositorio<Venta> repoVentas = new Repositorio<Venta>();
    static int contadorTickets = 1;

    static void Main(string[] args) {
        CargarProductos();
        CargarDatosBaseSiVacio();

        string opcion = "";
        do {
            Console.WriteLine("\nMenu de Punto de Tienda de Abarrotes la Pequeña\n");
            Console.WriteLine("1.-Productos \n2.-Punto de Venta \n3.-Inventario \n4.-Ventas \n5.-Salida \n");
            Console.Write("Que opcion deseas    : ");
            opcion = Console.ReadLine()?.Trim();

            switch (opcion) {
                case "1": MenuProductos(); break;
                case "2": MenuPuntoVenta(); break;
                case "3": MenuInventario(); break;
                case "4": ListarVentas(); break;
                case "5": GuardarDatos(); Console.WriteLine("Salida del Sistema y datos guardados."); break;
                default: Console.WriteLine("Opcion incorrecta"); break;
            }
        } while (opcion != "5");
    }

    static void MenuProductos() {
        string op = "";
        do {
            Console.WriteLine("\nOpciones de Productos\n\n1.-Modificar \n2.-Listado \n3.-Salida \n");
            Console.Write("Que opcion deseas    : ");
            op = Console.ReadLine()?.Trim();
            if (op == "1") ModificarProducto();
            else if (op == "2") ListarProductos();
        } while (op != "3");
    }

    static void ModificarProducto() {
        ListarProductos();
        Console.Write("\nIntroduce el codigo del producto a modificar : ");
        string id = Console.ReadLine()?.Trim();
        try {
            var p = repoProductos.ObtenerPorId(id);
            if (p == null) throw new ProductoNoEncontradoException("no existe el codigo");
            
            Console.Write($"Introduce el precio de {p.Id}   {p.Nombre} : ");
            if (double.TryParse(Console.ReadLine(), out double precio)) {
                p.Precio = precio;
                repoProductos.Actualizar(p);
                Console.WriteLine("Precio actualizado.");
            } else Console.WriteLine("no es un valor numerico");
        } catch (ProductoNoEncontradoException ex) { Console.WriteLine(ex.Message); }
    }

    static void ListarProductos() {
        Console.WriteLine("");
        foreach (var p in repoProductos.ObtenerTodos()) Console.WriteLine(p.ToString());
    }

    static void MenuPuntoVenta() {
        var carrito = new List<Producto>();
        string ticketId = contadorTickets++.ToString("D3");
        string fecha = DateTime.Now.ToString("dd-MM-yyyy");
        string op = "";

        do {
            Console.WriteLine($"\nFecha del Dia {fecha} Ticket No {ticketId}");
            Console.WriteLine("-----------------------------------------------------");
            double subtotal = 0;
            foreach (var item in carrito) {
                Console.WriteLine($"{item.Id}  {item.Nombre}  ${item.Precio}");
                subtotal += item.Precio;
            }
            Console.WriteLine("\n Menu de Punto de Venta\n\n1.-Agregar  \n2.-Eliminar \n3.-Listado \n4.-Pagar \n5.-Salida \n");
            Console.Write("Que opcion deseas : ");
            op = Console.ReadLine()?.Trim();

            if (op == "1") {
                ListarProductos();
                Console.Write("Introduce el codigo del producto: ");
                string id = Console.ReadLine()?.Trim();
                try {
                    var p = repoProductos.ObtenerPorId(id);
                    if (p == null) throw new ProductoNoEncontradoException("el codigo no existe no se puede agregar");
                    if (p.Stock <= 0) throw new StockInsuficienteException("no hay productos para venta");
                    
                    p.Stock--;
                    carrito.Add(new Producto(p.Id, p.Nombre, p.Precio, 1));
                    repoProductos.Actualizar(p);
                } catch (Exception e) { Console.WriteLine(e.Message); }
            } 
            else if (op == "4") {
                double iva = subtotal * 0.16;
                double total = subtotal + iva;
                Console.WriteLine($"\n El total sin iva {subtotal:F2}\n el iva total es {iva:F2}\n el total de la venta fue {total:F2}");
                repoVentas.Agregar(new Venta(ticketId, fecha, total));
                Console.WriteLine("Venta pagada y registrada.");
                op = "5";
            }
            else if (op == "5" && carrito.Any()) {
                Console.WriteLine("No pago el ticket. Devolviendo productos al stock...");
                foreach (var item in carrito) {
                    var pReal = repoProductos.ObtenerPorId(item.Id);
                    pReal.Stock++;
                    repoProductos.Actualizar(pReal);
                }
            }
        } while (op != "5");
    }

    static void MenuInventario() {
        string op = "";
        do {
            Console.WriteLine("\nOpciones de Inventarios\n\n1.-Listado \n2.-Agregar \n3.-Salida \n");
            Console.Write("Que opcion deseas    : ");
            op = Console.ReadLine()?.Trim();
            if (op == "1") ListarProductos();
            else if (op == "2") {
                ListarProductos();
                Console.Write("\nIntroduce el codigo del producto a modificar : ");
                string id = Console.ReadLine()?.Trim();
                var p = repoProductos.ObtenerPorId(id);
                if (p == null) Console.WriteLine("no existe el codigo");
                else {
                    Console.Write("\nIntroduce la Cantidad de Stock a Agregar : ");
                    if (int.TryParse(Console.ReadLine(), out int cant)) {
                        p.Stock += cant;
                        repoProductos.Actualizar(p);
                        Console.WriteLine("Stock actualizado.");
                    } else Console.WriteLine("no es un valor numerico");
                }
            }
        } while (op != "3");
    }

    static void ListarVentas() {
        Console.WriteLine("\n--- LISTADO DE VENTAS ---");
        if (repoVentas.Contar() == 0) Console.WriteLine("No hay ventas registradas.");
        foreach (var v in repoVentas.ObtenerTodos()) Console.WriteLine(v.ToString());
    }

    static void CargarDatosBaseSiVacio() {
        if (repoProductos.Contar() == 0) {
            Console.WriteLine("[INFO] Generando catálogo de 500 productos...");
            repoProductos.Agregar(new Producto("001", "Arroz 1kg", 35, 10));
            repoProductos.Agregar(new Producto("002", "Azúcar 1kg", 25, 10));
            repoProductos.Agregar(new Producto("003", "Harina 1kg", 28, 10));
            repoProductos.Agregar(new Producto("004", "Aceite 1L", 50, 10));
            repoProductos.Agregar(new Producto("005", "Leche 1L", 35, 10));
            repoProductos.Agregar(new Producto("006", "Huevos 12 unidades", 45, 10));
            repoProductos.Agregar(new Producto("007", "Fideos 500g", 20, 10));
            repoProductos.Agregar(new Producto("008", "Sal 1kg", 15, 10));
            repoProductos.Agregar(new Producto("009", "Pasta de tomate 400g", 25, 10));
            repoProductos.Agregar(new Producto("010", "Atún lata 170g", 35, 10));
            for (int i = 11; i <= 500; i++) repoProductos.Agregar(new Producto(i.ToString("D3"), $"Producto Zorro {i}", 15.0 + (i%20), 50));
        }
    }

    static void GuardarDatos() {
        File.WriteAllLines("productos.csv", repoProductos.ObtenerTodos().Select(p => p.ALineaCSV()));
        File.WriteAllLines("ventas.csv", repoVentas.ObtenerTodos().Select(v => v.ALineaCSV()));
    }
    static void CargarProductos() {
        if (!File.Exists("productos.csv")) return;
        foreach (var linea in File.ReadAllLines("productos.csv")) {
            var d = linea.Split(',');
            if (d.Length == 4) repoProductos.Agregar(new Producto(d[0], d[1], double.Parse(d[2]), int.Parse(d[3])));
        }
    }
}
