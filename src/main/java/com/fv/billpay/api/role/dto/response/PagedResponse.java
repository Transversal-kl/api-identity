package com.fv.billpay.api.role.dto.response;

import java.util.List;

/**
 * DTO genérico para respuestas paginadas.
 * Proporciona metadatos de paginación útiles para el frontend.
 * 
 * @param <T> Tipo de elementos en la lista
 */
public class PagedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;

    public PagedResponse(List<T> content, long totalElements, int page, int size) {
        if (content == null) {
            throw new IllegalArgumentException("content no puede ser null");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements debe ser mayor o igual a 0");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page debe ser mayor o igual a 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size debe ser mayor a 0");
        }
        
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }

    // Getters
    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return page >= totalPages - 1;
    }

    public boolean hasNext() {
        return page < totalPages - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }
}
