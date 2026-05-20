# ProductoIntegrador_POO
Producto Integrador: Sistema POS Orientado a Objetos en Java, C# y Python.

# Sistema de Punto de Venta (POS) e Inventario - Producto Integrador

**Estudiante:** [Oliver Israel Falcon Solano]  
**Matrícula:** [a2233336128]  
**Carrera:** Ingeniería en Sistemas  
**Materia:** Programación Orientada a Objetos  

---

## 📝 Descripción del Proyecto
Este proyecto consiste en el desarrollo y reingeniería de un sistema de **Punto de Venta (POS) y Gestión de Inventarios** llevado a un estándar arquitectónico profesional de programación orientada a objetos. 

El sistema implementa:
- **Abstracción y Herencia:** Uso de una clase base común para la gestión de identidades.
- **Genericidad (Generics):** Creación de un `Repositorio<T>` genérico capaz de manejar cualquier entidad de datos de forma dinámica en colecciones (`List`).
- **Robustez (Manejo de Excepciones):** Control estricto de errores mediante excepciones personalizadas (`ProductoNoEncontradoException`) para evitar fallos en tiempo de ejecución.

---

## 📁 Estructura del Repositorio

El código está desarrollado de manera idéntica en lógica y estructura en tres lenguajes de programación en modo consola:

* **`Java/`**: Implementación principal utilizando Java Collections Framework (`ArrayList`, `Optional`, `Streams`).
* **`CSharp/`**: Traducción estricta a C# utilizando LINQ (`IEnumerable`, `FirstOrDefault`).
* **`Python/`**: Adaptación a Python utilizando tipado estricto con el módulo `typing` (`Generic`, `TypeVar`).

---

## 🚀 Cómo Ejecutar los Proyectos
Cualquiera de los tres archivos principales (`SistemaPOS.java`, `Program.cs` o `main.py`) puede ser copiado y ejecutado directamente en entornos locales o compiladores online en modo consola (como OnlineGDB o Replit).
