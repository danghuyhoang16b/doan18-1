package com.example.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserListResponse {
    @SerializedName("data")
    private List<User> data;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<User> getData() { return data; }
    public Pagination getPagination() { return pagination; }

    public static class Pagination {
        @SerializedName("current_page")
        private int currentPage;
        @SerializedName("total_pages")
        private int totalPages;
        @SerializedName("total_records")
        private int totalRecords;
        @SerializedName("limit")
        private int limit;

        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }
        public int getTotalRecords() { return totalRecords; }
        public int getLimit() { return limit; }
    }
}
