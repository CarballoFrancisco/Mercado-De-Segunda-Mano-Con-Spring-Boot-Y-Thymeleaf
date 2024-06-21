package com.openwebinars.secondhandmarket.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openwebinars.secondhandmarket.modelo.Compra;
import com.openwebinars.secondhandmarket.modelo.Producto;
import com.openwebinars.secondhandmarket.modelo.Usuario;
import com.openwebinars.secondhandmarket.repositorios.CompraRepository;

@Service
public class CompraServicio {
	
	@Autowired
	CompraRepository repositorio;
	
	@Autowired
	ProductoServicio productoServicio;
	
	public Compra insertar(Compra c, Usuario u) {
		c.setPropietario(u);
		return repositorio.save(c);
	}
	
	public Compra insertar(Compra c) {
		return repositorio.save(c);
	}
	
//Supongamos que el método productoServicio.editar(p) guarda o actualiza el producto p en la base de datos. 
//Cuando guardas o actualizas un producto que tiene una compra asociada (compra no es null), JPA se encarga de persistir la relación en la base de datos.
//Por lo tanto, aunque solo estás modificando la relación compra en la instancia de Producto con p.setCompra(c),
//al guardar el producto con productoServicio.editar(p), JPA se asegurará de que la relación entre el producto y la compra se refleje correctamente en la base de datos.
// a modo de resumen cuando editamos o gurdamos el producto, JPA se encargará de buscar la relación automaticamente y guardarla
	public Producto addProductoCompra(Producto p, Compra c) {
		p.setCompra(c);
		return productoServicio.editar(p);
	}
	
	public Compra buscarPorId(long id) {
		return repositorio.findById(id).orElse(null);
	}
	
	public List<Compra> todas() {
		return repositorio.findAll();
	}
	
	public List<Compra> porPropietario(Usuario u) {
		return repositorio.findByPropietario(u);
	}
	
}
