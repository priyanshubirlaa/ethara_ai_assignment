package com.hotel.book.dto;



import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private List<T> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;
}
