package com.pricemonitor.service;

import com.pricemonitor.model.Products;
import com.pricemonitor.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductsRepository productsRepository;

	public List<Products> getAllActiveProducts() {
		return productsRepository.findByIsActiveTrue();
	}

	public Optional<Products> getProductById(UUID id) {
		return productsRepository.findById(id);
	}

	public List<Products> getProductsByCategory(String category) {
		return productsRepository.findByCategoryAndIsActiveTrue(category);
	}

	public List<Products> searchProducts(String query) {
		return productsRepository.searchProducts(query);
	}

	public Products saveProduct(Products product) {
		return productsRepository.save(product);
	}

	public void deleteProduct(UUID id) {
		productsRepository.deleteById(id);
	}

	public Optional<Products> findByBlsSeriesId(String blsSeriesId) {
		return productsRepository.findByBlsSeriesId(blsSeriesId);
	}

	public Optional<Products> findByBlsItemCode(String blsItemCode) {
		return productsRepository.findByBlsItemCode(blsItemCode);
	}
}
