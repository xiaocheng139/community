package com.xcfei.community.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class Page {
    private int current = 1;

    private int limit = 10;

    private int totalRows;

    @Setter
    private String path;
    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }
    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }
    public void setTotalRows(int totalRows) {
        if (totalRows >= 0) {
            this.totalRows = totalRows;
        }
    }

    /**
     * Get the initial row number of current page
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * Get total page size
     */
    public int getNumberOfPages() {
        return (int) Math.ceil((double) totalRows / limit);
    }

    /**
     * Get the start page number of current page window
     */
    public int getPageWindowStart() {
        return Math.max(current - 2, 1);
    }

    /**
     * Get the end page number of current page window
     */
    public int getPageWindowEnd() {
        return Math.min(current + 2, getNumberOfPages());
    }
}
