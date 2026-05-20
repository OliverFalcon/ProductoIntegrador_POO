import os
from abc import ABC, abstractmethod
from datetime import datetime
from typing import TypeVar, Generic, List, Optional

# 1. EXCEPCIONES PERSONALIZADAS
class ProductoNoEncontradoException(Exception): pass
class StockInsuficienteException(Exception): pass

# 2. ENTIDADES BASE
class EntidadBase(ABC):
    def __init__(self, id_val: str):
        self.id = id_val

    def __eq__(self, other):
        if isinstance(other, EntidadBase): return self.id == other.id
        return False

    def __hash__(self): return hash(self.id)

    @abstractmethod
    def a_linea_csv(self) -> str: pass

class Producto(EntidadBase):
    def __init__(self, id_val: str, nombre: str, precio: float, stock: int):
        super().__init__(id_val)
        self.nombre = nombre
        self.precio = precio
        self.stock = stock

    def a_linea_csv(self) -> str:
        return f"{self.id},{self.nombre},{self.precio},{self.stock}"

    def __str__(self):
        return f"{self.id:<5} {self.nombre:<25} ${self.precio:<10.2f} {self.stock:<10}"

class Venta(EntidadBase):
    def __init__(self, id_val: str, fecha: str, total: float):
        super().__init__(id_val)
        self.fecha = fecha
        self.total = total

    def a_linea_csv(self) -> str:
        return f"{self.id},{self.fecha},{self.total}"

    def __str__(self):
        return f"Ticket: {self.id:<5} | Fecha: {self.fecha:<12} | Total: ${self.total:.2f}"

# 3. REPOSITORIO GENÉRICO
T = TypeVar('T', bound=EntidadBase)
class Repositorio(Generic[T]):
    def __init__(self):
        self.elementos: List[T] = []

    def agregar(self, elemento: T): self.elementos.append(elemento)
    def obtener_todos(self) -> List[T]: return list(self.elementos)
    def contar(self) -> int: return len(self.elementos)
    
    def obtener_por_id(self, id_val: str) -> Optional[T]:
        for e in self.elementos:
            if e.id == id_val: return e
        return None

    def actualizar(self, objeto: T) -> bool:
        for i, e in enumerate(self.elementos):
            if e.id == objeto.id:
                self.elementos[i] = objeto
                return True
        return False

# 4. PROGRAMA PRINCIPAL
repo_productos = Repositorio[Producto]()
repo_ventas = Repositorio[Venta]()
contador_tickets = 1

def cargar_productos():
    if not os.path.exists("productos.csv"): return
    with open("productos.csv", "r") as f:
        for linea in f:
            d = linea.strip().split(',')
            if len(d) == 4: repo_productos.agregar(Producto(d[0], d[1], float(d[2]), int(d[3])))

def guardar_datos():
    with open("productos.csv", "w") as f:
        for p in repo_productos.obtener_todos(): f.write(p.a_linea_csv() + "\n")
    with open("ventas.csv", "w") as f:
        for v in repo_ventas.obtener_todos(): f.write(v.a_linea_csv() + "\n")

def cargar_datos_base_si_vacio():
    if repo_productos.contar() == 0:
        print("[INFO] Generando catálogo de 500 productos...")
        repo_productos.agregar(Producto("001", "Arroz 1kg", 35.0, 10))
        repo_productos.agregar(Producto("002", "Azúcar 1kg", 25.0, 10))
        repo_productos.agregar(Producto("003", "Harina 1kg", 28.0, 10))
        repo_productos.agregar(Producto("004", "Aceite 1L", 50.0, 10))
        repo_productos.agregar(Producto("005", "Leche 1L", 35.0, 10))
        repo_productos.agregar(Producto("006", "Huevos 12 unidades", 45.0, 10))
        repo_productos.agregar(Producto("007", "Fideos 500g", 20.0, 10))
        repo_productos.agregar(Producto("008", "Sal 1kg", 15.0, 10))
        repo_productos.agregar(Producto("009", "Pasta de tomate 400g", 25.0, 10))
        repo_productos.agregar(Producto("010", "Atún lata 170g", 35.0, 10))
        for i in range(11, 501):
            repo_productos.agregar(Producto(f"{i:03d}", f"Producto Zorro {i}", 15.0 + (i%20), 50))

def listar_productos():
    print("")
    for p in repo_productos.obtener_todos(): print(p)

def modificar_producto():
    listar_productos()
    id_val = input("\nIntroduce el codigo del producto a modificar : ").strip()
    p = repo_productos.obtener_por_id(id_val)
    if not p:
        print("no existe el codigo")
        return
    precio_str = input(f"Introduce el precio de {p.id}   {p.nombre} : ")
    try:
        p.precio = float(precio_str)
        repo_productos.actualizar(p)
        print("Precio actualizado.")
    except ValueError:
        print("no es un valor numerico")

def menu_productos():
    op = ""
    while op != "3":
        print("\nOpciones de Productos\n\n1.-Modificar \n2.-Listado \n3.-Salida \n")
        op = input("Que opcion deseas    : ").strip()
        if op == "1": modificar_producto()
        elif op == "2": listar_productos()

def menu_punto_venta():
    global contador_tickets
    carrito = []
    ticket_id = f"{contador_tickets:03d}"
    contador_tickets += 1
    fecha = datetime.now().strftime("%dd-%m-%Y")
    op = ""

    while op != "5":
        print(f"\nFecha del Dia {fecha} Ticket No {ticket_id}")
        print("-" * 53)
        subtotal = 0.0
        for item in carrito:
            print(f"{item.id}  {item.nombre}  ${item.precio}")
            subtotal += item.precio
        
        print("\n Menu de Punto de Venta\n\n1.-Agregar  \n2.-Eliminar \n3.-Listado \n4.-Pagar \n5.-Salida \n")
        op = input("Que opcion deseas : ").strip()

        if op == "1":
            listar_productos()
            id_val = input("Introduce el codigo del producto: ").strip()
            p = repo_productos.obtener_por_id(id_val)
            if not p:
                print("el codigo no existe no se puede agregar")
            elif p.stock <= 0:
                print("no hay productos para venta")
            else:
                p.stock -= 1
                carrito.append(Producto(p.id, p.nombre, p.precio, 1))
                repo_productos.actualizar(p)
        elif op == "4":
            iva = subtotal * 0.16
            total = subtotal + iva
            print(f"\n El total sin iva {subtotal:.2f}\n el iva total es {iva:.2f}\n el total de la venta fue {total:.2f}")
            repo_ventas.agregar(Venta(ticket_id, fecha, total))
            print("Venta pagada y registrada.")
            op = "5"
        elif op == "5" and carrito:
            print("No pago el ticket. Devolviendo productos al stock...")
            for item in carrito:
                p_real = repo_productos.obtener_por_id(item.id)
                p_real.stock += 1
                repo_productos.actualizar(p_real)

def menu_inventario():
    op = ""
    while op != "3":
        print("\nOpciones de Inventarios\n\n1.-Listado \n2.-Agregar \n3.-Salida \n")
        op = input("Que opcion deseas    : ").strip()
        if op == "1": listar_productos()
        elif op == "2":
            listar_productos()
            id_val = input("\nIntroduce el codigo del producto a modificar : ").strip()
            p = repo_productos.obtener_por_id(id_val)
            if not p: print("no existe el codigo")
            else:
                cant_str = input("\nIntroduce la Cantidad de Stock a Agregar : ")
                try:
                    p.stock += int(cant_str)
                    repo_productos.actualizar(p)
                    print("Stock actualizado.")
                except ValueError: print("no es un valor numerico")

def listar_ventas():
    print("\n--- LISTADO DE VENTAS ---")
    if repo_ventas.contar() == 0: print("No hay ventas registradas.")
    for v in repo_ventas.obtener_todos(): print(v)

def main():
    cargar_productos()
    cargar_datos_base_si_vacio()
    opcion = ""
    while opcion != "5":
        print("\nMenu de Punto de Tienda de Abarrotes la Pequeña\n")
        print("1.-Productos \n2.-Punto de Venta \n3.-Inventario \n4.-Ventas \n5.-Salida \n")
        opcion = input("Que opcion deseas    : ").strip()

        if opcion == "1": menu_productos()
        elif opcion == "2": menu_punto_venta()
        elif opcion == "3": menu_inventario()
        elif opcion == "4": listar_ventas()
        elif opcion == "5":
            guardar_datos()
            print("Salida del Sistema y datos guardados.")
        else: print("Opcion incorrecta")

if __name__ == "__main__":
    main()
