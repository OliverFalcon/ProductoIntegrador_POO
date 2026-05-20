using System;
using System.Collections.Generic;
using System.Linq;

namespace SistemaPOS
{
    // 1. Excepción Personalizada
    public class ProductoNoEncontradoException : Exception
    {
        public ProductoNoEncontradoException(string mensaje) : base(mensaje) { }
    }

    // 2. Entidad Base
    public abstract class EntidadBase
    {
        public string Id { get; set; }
    }

    // 3. Entidad Concreta
    public class Producto : EntidadBase
    {
        public string Nombre { get; set; }
        public double Precio { get; set; }
        public int Stock { get; set; }

        public Producto(string id, string nombre, double precio, int stock)
        {
            Id = id;
            Nombre = nombre;
            Precio = precio;
            Stock = stock;
        }

        public override string ToString()
        {
            return $"ID: {Id} | {Nombre} | ${Precio:F2} | Stock: {Stock}";
        }
    }

    // 4. Repositorio Genérico
    public class Repositorio<T> where T : EntidadBase
    {
        private readonly List<T> elementos = new List<T>();

        public void Agregar(T elemento)
        {
            elementos.Add(elemento);
        }

        public List<T> ObtenerTodos()
        {
            return new List<T>(elementos);
        }

        public T ObtenerPorId(string id)
        {
            return elementos.FirstOrDefault(e => e.Id == id);
        }

        public bool EliminarPorId(string id)
        {
            T elemento = ObtenerPorId(id);
            if (elemento == null)
            {
                throw new ProductoNoEncontradoException($"Error: El ID {id} no existe.");
            }
            return elementos.Remove(elemento);
        }
    }

    // 5. Clase Principal
    class Program
    {
        static void Main(string[] args)
        {
            Repositorio<Producto> inventario = new Repositorio<Producto>();
            inventario.Agregar(new Producto("001", "Arroz 1kg", 35.0, 10));
            inventario.Agregar(new Producto("002", "Azúcar 1kg", 25.0, 10));

            int opcion = 0;

            while (opcion != 4)
            {
                Console.WriteLine("\n--- SISTEMA POS E INVENTARIO ---");
                Console.WriteLine("1. Mostrar Catálogo");
                Console.WriteLine("2. Agregar Producto");
                Console.WriteLine("3. Eliminar Producto");
                Console.WriteLine("4. Salir");
                Console.Write("Elige una opción: ");
                
                if (int.TryParse(Console.ReadLine(), out opcion))
                {
                    try
                    {
                        switch (opcion)
                        {
                            case 1:
                                Console.WriteLine("\n--- INVENTARIO ACTUAL ---");
                                foreach (var p in inventario.ObtenerTodos())
                                    Console.WriteLine(p.ToString());
                                break;
                            case 2:
                                Console.Write("ID: "); string id = Console.ReadLine();
                                Console.Write("Nombre: "); string nom = Console.ReadLine();
                                Console.Write("Precio: "); double pre = double.Parse(Console.ReadLine());
                                Console.Write("Stock: "); int st = int.Parse(Console.ReadLine());
                                inventario.Agregar(new Producto(id, nom, pre, st));
                                Console.WriteLine("¡Agregado exitosamente!");
                                break;
                            case 3:
                                Console.Write("Introduce el ID a eliminar: ");
                                string idElim = Console.ReadLine();
                                inventario.EliminarPorId(idElim);
                                Console.WriteLine("Producto eliminado.");
                                break;
                        }
                    }
                    catch (ProductoNoEncontradoException e)
                    {
                        Console.WriteLine(e.Message);
                    }
                }
            }
        }
    }
}