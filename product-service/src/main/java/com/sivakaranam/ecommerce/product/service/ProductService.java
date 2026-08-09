package com.sivakaranam.ecommerce.product.service;

import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.product.dto.ProductRequest;
import com.sivakaranam.ecommerce.product.model.Category;
import com.sivakaranam.ecommerce.product.model.Product;
import com.sivakaranam.ecommerce.product.repository.ProductRepository;
import com.sivakaranam.ecommerce.product.search.ProductDocument;
import com.sivakaranam.ecommerce.product.search.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductSearchRepository productSearchRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryService categoryService,
            ProductSearchRepository productSearchRepository
    ) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productSearchRepository = productSearchRepository;
    }

    @Transactional
    public Product create(ProductRequest request) {
        Category category = categoryService.findById(request.categoryId());

        Product product = new Product();
        applyRequest(product, request, category);
        Product saved = productRepository.save(product);

        indexInSearch(saved);
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        Category category = categoryService.findById(request.categoryId());
        applyRequest(product, request, category);
        Product saved = productRepository.save(product);

        indexInSearch(saved);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
        removeFromSearch(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product with id " + id));
    }

    public Page<Product> list(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> listByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<ProductDocument> search(String query, Pageable pageable) {
        return productSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
    }

    private void applyRequest(Product product, ProductRequest request, Category category) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
    }

    /**
     * Elasticsearch is kept in sync on write, not via a batch job. Simple
     * enough for this scope, though it does mean a failed index write leaves
     * MySQL and Elasticsearch briefly inconsistent. Logged rather than thrown,
     * since a search-index hiccup shouldn't fail the actual product write.
     */
    private void indexInSearch(Product product) {
        try {
            ProductDocument document = new ProductDocument(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getPrice()
            );
            productSearchRepository.save(document);
        } catch (Exception e) {
            log.error("Failed to index product {} in Elasticsearch", product.getId(), e);
        }
    }

    private void removeFromSearch(Long id) {
        try {
            productSearchRepository.deleteById(id.toString());
        } catch (Exception e) {
            log.error("Failed to remove product {} from Elasticsearch", id, e);
        }
    }
}
