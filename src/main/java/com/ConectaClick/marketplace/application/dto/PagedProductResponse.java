package com.ConectaClick.marketplace.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PagedProductResponse {
    private List<ProductDTO> products;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
