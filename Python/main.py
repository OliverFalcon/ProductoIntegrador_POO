from typing import TypeVar, Generic, List, Optional

# 1. Excepción Personalizada
class ProductoNoEncontradoException(Exception):
    pass

# 2. Entidad Base
class EntidadBase:
    def __init__(self, id_val: str):
        self.id = id_val

# 3. Entidad Concreta
class Producto(EntidadBase):
    def __init__(self, id_val: str, nombre: str, precio: float, stock: int):
        super().__init__(id_val)
        self.nombre = nombre
        self.precio = precio
        self.stock = stock

    def __str__(self):
        return f"ID: {self.id} | {self.nombre} | ${self.precio:.2f} | Stock: {self.stock}"

# Variable de tipo para la Genericidad
T = TypeVar('T', bound=EntidadBase)

# 4. Repositorio Genérico
class Repositorio(Generic[T]):
    def __init__(self):
        self._elementos: List[T] = []

    def agregar(self, elemento: T) -> None:
        self._elementos.append(elemento)

    def obtener_todos(self) -> List[T]:
        return list(self._elementos)

    def obtener_por_id(self, id_val: str) -> Optional[T]:
        for e in self._elementos:
            if e.id == id_val:
                return e
        return None

    def eliminar_por_id(self, id_val: str) -> bool:
        elemento = self.obtener_por_id(id_val)
        if elemento is None:
            raise ProductoNoEncontradoException(f"Error: El ID {id_val} no existe.")
        self._elementos.remove(elemento)
        return True

# 5. Programa Principal
def main():
    inventario = Repositorio[Producto]()
    inventario.agregar(Producto("001", "Arroz 1kg", 35.0, 10))
    inventario.agregar(Producto("002", "Azúcar 1kg", 25.0, 10))

    opcion = 0
    while opcion != 4:
        print("\n--- SISTEMA POS E INVENTARIO ---")
        print("1. Mostrar Catálogo")
        print("2. Agregar Producto")
        print("3. Eliminar Producto")
        print("4. Salir")
        
        try:
            opcion = int(input("Elige una opción: "))
            
            if opcion == 1:
                print("\n--- INVENTARIO ACTUAL ---")
                for p in inventario.obtener_todos():
                    print(p)
            elif opcion == 2:
                id_val = input("ID: ")
                nombre = input("Nombre: ")
                precio = float(input("Precio: "))
                stock = int(input("Stock: "))
                inventario.agregar(Producto(id_val, nombre, precio, stock))
                print("¡Agregado exitosamente!")
            elif opcion == 3:
                id_elim = input("Introduce el ID a eliminar: ")
                inventario.eliminar_por_id(id_elim)
                print("Producto eliminado.")
                
        except ProductoNoEncontradoException as e:
            print(e)
        except ValueError:
            print("Por favor, ingresa un valor válido.")

if __name__ == "__main__":
    main()