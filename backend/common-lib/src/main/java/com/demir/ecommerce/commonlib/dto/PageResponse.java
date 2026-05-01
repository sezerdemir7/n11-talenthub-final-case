package com.demir.ecommerce.commonlib.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PageResponse<T> implements Serializable {

    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean empty;

    private PageResponse(List<T> content,
                         int pageNumber,
                         int pageSize,
                         long totalElements,
                         int totalPages,
                         boolean first,
                         boolean last,
                         boolean empty) {

        this.content = content == null ? List.of() : List.copyOf(content);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.empty = empty;
    }

    public static <T> PageResponse<T> of(List<T> content,
                                         int pageNumber,
                                         int pageSize,
                                         long totalElements,
                                         int totalPages,
                                         boolean first,
                                         boolean last,
                                         boolean empty) {

        return new PageResponse<>(
                content,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                first,
                last,
                empty
        );
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isEmpty() {
        return empty;
    }
}