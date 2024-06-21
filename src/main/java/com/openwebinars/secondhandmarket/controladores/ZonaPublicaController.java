package com.openwebinars.secondhandmarket.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.openwebinars.secondhandmarket.modelo.Producto;
import com.openwebinars.secondhandmarket.servicios.ProductoServicio;

@Controller
@RequestMapping("/public")
public class ZonaPublicaController {
	
	@Autowired
	ProductoServicio productoServicio;
	
//@ModelAttribute es una anotación de Spring MVC que se utiliza para agregar atributos al modelo para ser utilizados por las vistas.
//"productos" es el nombre del atributo que se añadirá al modelo.
//Este es el método que devuelve el valor del atributo productos. Devuelve una lista de objetos Producto que representan los
//productos que no han sido vendidos.
// esta función se usa para mostrar la lista de productos en la vista
	@ModelAttribute("productos")
//@ModelAttribute("productos")En resumen, @ModelAttribute se utiliza para asegurar que ciertos datos estén siempre presentes en el modelo
	public List<Producto> productosNoVendidos() {
		return productoServicio.productosSinVender();
	}
	
//@GetMapping({"/", "/index"}): Esta anotación indica que este método maneja las solicitudes HTTP GET para las rutas / y /index.
//RequestParam(name="q", required=false, esto indica que el parametro que se vincula directamente con la solicitud http no es obligatorio
//posteriormente guardamos en el modelo el resultado de la query y la mostramos en la vista
	@GetMapping({"/", "/index"})
	public String index(Model model, @RequestParam(name="q", required=false) String query) {
		if (query != null)
			model.addAttribute("productos", productoServicio.buscar(query));
		return "index";
	}

	// buscamos el producto x su id y si lo encontramos lo añadimos al modelo para visualizarlo y en otro caso
	// lo redirigimos a la página de public
	@GetMapping("/producto/{id}")
	public String showProduct(Model model, @PathVariable Long id) {
		Producto result = productoServicio.findById(id); 
		if (result != null) {
			model.addAttribute("producto", result);
			return "producto";
		}
		return "redirect:/public";
	}
	
	

}
