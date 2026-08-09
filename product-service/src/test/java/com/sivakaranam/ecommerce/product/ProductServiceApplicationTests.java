package com.sivakaranam.ecommerce.product;

import com.sivakaranam.ecommerce.product.search.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceApplicationTests {

    // Real Elasticsearch repositories ping the cluster and check the index
    // exists at bean-creation time, so a context test needs this mocked out
    // unless Elasticsearch is actually running alongside the test.
    @MockBean
    private ProductSearchRepository productSearchRepository;

    @Test
    void contextLoads() {
    }
}
