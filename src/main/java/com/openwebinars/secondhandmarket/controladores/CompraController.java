package com.openwebinars.secondhandmarket.controladores;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.openwebinars.secondhandmarket.modelo.Compra;
import com.openwebinars.secondhandmarket.modelo.Producto;
import com.openwebinars.secondhandmarket.modelo.Usuario;
import com.openwebinars.secondhandmarket.servicios.CompraServicio;
import com.openwebinars.secondhandmarket.servicios.ProductoServicio;
import com.openwebinars.secondhandmarket.servicios.UsuarioServicio;

@Controller
//La anotación @RequestMapping("/app") en Spring MVC tiene el propósito de mapear una clase de controlador ahora todo los metodos de la clase
//estarán agrupados bajos el prefijo app.
@RequestMapping("/app")
public class CompraController {
	
	@Autowired
	CompraServicio compraServicio;
	
	@Autowired
	ProductoServicio productoServicio;
	
	@Autowired
	UsuarioServicio usuarioServicio;
	
	@Autowired
	HttpSession session;
	
private Usuario usuario;
//La anotación @ModelAttribute("carrito") indica que el método productosCarrito() debe ejecutarse cada vez que se prepara el modelo de una vista, y el resultado de este método se almacena en el modelo bajo el nombre "carrito"
//En resumen, @ModelAttribute se utiliza para asegurar que ciertos datos estén siempre presentes en el modelo, mientras que Model
//.addAttribute se utiliza para agregar datos al modelo dinámicamente en función de las condiciones específicas de la solicitud.
// El carrito de compra lo sacamos de la sesión, y lo que sacaremos es una lista de id de productos que queremos comprar posteriormente
// con el servicio de productos obtendremos un array de productos buscados x su id. en el caso que el carro esté vacio no devolverá nada
	@ModelAttribute("carrito")
	public List<Producto> productosCarrito() {
		List<Long> contenido = (List<Long>) session.getAttribute("carrito");
		return (contenido == null) ? null : productoServicio.variosPorId(contenido);
	}
//Obtenemos todos los productos del carrito	por medio de la llamada a la función productosCarrito si el carrito está vacio devolverá null
//El método stream() convierte la lista productosCarrito en un stream de objetos Producto. Este stream permite realizar operaciones 
//funcionales sobre los elementos de la lista. y devuelve un numero de tipo double.

	@ModelAttribute("total_carrito")
	public Double totalCarrito() {
		List<Producto> productosCarrito = productosCarrito();
		if (productosCarrito != null)
			return productosCarrito.stream()
//Es una operación intermedia de la API de Streams de Java que se utiliza para transformar cada elemento de un stream en un valor double.
//en este caso traforma el precio en un doble y posteriormente los suma
				.mapToDouble(p -> p.getPrecio())
				.sum();
		return 0.0;
	}
	
//@ModelAttribute es una anotación de Spring MVC que se utiliza para vincular un método a un modelo, permitiendo que sus resultados estén disponibles para las vistas.	
	@ModelAttribute("mis_compras")
	public List<Compra> misCompras() {
//SecurityContextHolder.getContext().getAuthentication().getName() devuelve el nombre del usuario actualmente autenticado en el 
//sistema. En este caso, se está utilizando para obtener el email del usuario autenticado.
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
// Es un método que busca y devuelve el usuario basado en su dirección de correo electrónico.
		usuario = usuarioServicio.buscarPorEmail(email);
//Finalmente, la función retorna la lista de compras del usuario autenticado.
		return compraServicio.porPropietario(usuario);
	}
	
// Nos redirige a la vista del carrito
	@GetMapping("/carrito")
	public String verCarrito(Model model) {
		return "app/compra/carrito";
	}
	
//Esta anotación indica que este método manejará las solicitudes GET a la ruta /app/carrito/add/{id} donde {id} es el 
//identificador del producto que se va a agregar al carrito. {id} se va a extraer de la URL y se va a asignar a la variable id
//model model. Este parámetro se utiliza para agregar atributos al modelo, pero en este caso particular no se está utilizando para agregar atributos adicionales al modelo.	
	@GetMapping("/carrito/add/{id}")
	
	public String addCarrito(Model model, @PathVariable Long id) {
//Se obtiene el contenido actual del carrito de la sesión. Este contenido es una lista de IDs de productos que están en el carrito.		
		List<Long> contenido = (List<Long>) session.getAttribute("carrito");
//Si la lista contenido es null (es decir, si el carrito está vacío o no ha sido inicializado antes), se inicializa como una nueva lista vacía.
		if (contenido == null)
			contenido = new ArrayList<>();
//Se verifica si el producto con el ID id ya está en el carrito. Si no está presente, se agrega a la lista contenido.
		if (!contenido.contains(id))
			contenido.add(id);
//Se actualiza el atributo "carrito" en la sesión con la nueva lista contenido que puede incluir el producto recién agregado
		session.setAttribute("carrito", contenido);
//Después de agregar el producto al carrito y actualizar la sesión, se redirige al usuario de vuelta a la página del carrito (/app/carrito).		
		return "redirect:/app/carrito";
	}
	
//@GetMapping("/carrito/eliminar/{id}") Esta anotación indica que este método manejará las solicitudes GET a la ruta 
//app/carrito/eliminar/{id} donde {id} es el identificador del producto que se va a eliminar del carrito.
	@GetMapping("/carrito/eliminar/{id}")
	public String borrarDeCarrito(Model model, @PathVariable Long id) {
	    List<Long> contenido = (List<Long>) session.getAttribute("carrito");

	    // Si el contenido del carrito es null, no se puede eliminar ningún producto,
	    // por lo tanto, redirigimos al usuario a la página /public.
	    if (contenido == null) {
	        return "redirect:/public";
	    }
	    
	    // Remover el producto del carrito basado en el id proporcionado
	    contenido.remove(id);

	    // Actualizar la sesión con el contenido actualizado del carrito
// si el carrito está vacio destruimos la sesion
	    if (contenido.isEmpty()) {
	        session.removeAttribute("carrito");
	    } else {
// si no está vacio lo actualizamos
	        session.setAttribute("carrito", contenido);
	    }

	 // Redirigir de vuelta a la página del carrito
	    return "redirect:/app/carrito";
	}

	
	@GetMapping("/carrito/finalizar")
	public String checkout() {
//Obtiene los id  del carrito de la sesión 
		List<Long> contenido = (List<Long>) session.getAttribute("carrito");
//Si el contenido del carrito es null, redirige al usuario a la página /public
		if (contenido == null)
			return "redirect:/public";
//aqui llamamos a la función productosCarrito el cual x medio de la sesión nos dará los productos x medio de los id		
		List<Producto> productos = productosCarrito();
//new Compra(): Crea una nueva instancia de Compra, que inicialmente no tiene productos asociados ni otros datos.
//usuario: Especifica el usuario que está realizando la compra

// La variable c ahora contendrá la instancia de Compra que ha sido guardada en la base de datos. Esta instancia incluirá el ID 
//generado automáticamente y cualquier otro dato que se haya establecido o mapeado durante el proceso de guardado como x ejemplo
// la fecha de compra del producto.
		Compra c = compraServicio.insertar(new Compra(), usuario);
// Aqui asignamos a cada producto la compra a la q pertenecen		
		productos.forEach(p -> compraServicio.addProductoCompra(p, c));
// x ultimo destruimos la sesión  y no redirigimos hacia la factura
		session.removeAttribute("carrito");

		return "redirect:/app/compra/factura/"+c.getId();
		
	}
	
//Esta función verMisCompras es un controlador de Spring MVC que maneja solicitudes HTTP GET para la ruta /miscompras
	@GetMapping("/miscompras")
	public String verMisCompras(Model model) {
		return "/app/compra/listado";
	}
	
//Esta anotación indica que el método factura responderá a las solicitudes HTTP GET dirigidas a la URL 
// /compra/factura/{id}, donde {id} es un parámetro de ruta que representa el identificador de una compra específica..	
	@GetMapping("/compra/factura/{id}")
//Model model: Es una interfaz de Spring que se utiliza para pasar datos desde el controlador a la vista.
// @PathVariable Long id: Anotación que indica que el valor del parámetro id en la URL debe ser vinculado a este argumento del método. Este id representa el identificador de una compra específica.	
	public String factura(Model model, @PathVariable Long id) {
// en c obtenemos la compra x su id		
		Compra c = compraServicio.buscarPorId(id);
//se obtiene una lista de productos asociados con la compra c
		List<Producto> productos = productoServicio.productosDeUnaCompra(c);
// guardamos la compra y los productos en el modelo para mostrarlos posteriormente		
		model.addAttribute("productos", productos);
		model.addAttribute("compra", c);
//Calcula el total de la compra sumando los precios de todos los productos y añade este total al modelo con el atributo total_compra.
		model.addAttribute("total_compra", productos.stream().mapToDouble(p -> p.getPrecio()).sum());
//y nos redireccionará a esta vista
		return "/app/compra/factura";
	}
}

