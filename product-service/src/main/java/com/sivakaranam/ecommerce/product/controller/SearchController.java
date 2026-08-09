package com.sivakaranam.ecommerce.product.controller;

import com.sivakaranam.ecommerce.product.search.ProductDocument;
import com.sivakaranam.ecommerce.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Full-text product search backed by Elasticsearch, separate from the plain
 * paginated /api/products listing which reads straight from MySQL.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductDocument> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return productService.search(q, pageable);
    }
}
