package com.heybcat.docker.pull.web.entity.view;

import java.util.List;

/**
 * @author Fetters
 */
public class HubSearchView {

    /**
     * 搜索结果总数
     */
    private Integer total;
    /**
     * 当前页大小
     */
    private Integer size;

    private List<ResultView> results;

    public HubSearchView(List<ResultView> resultViews, Integer total, int size) {
        this.results = resultViews;
        this.total = total;
        this.size = size;
    }

    public List<ResultView> getResults() {
        return results;
    }

    public void setResults(
        List<ResultView> results) {
        this.results = results;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public static class ResultView{

        private String id;

        private String name;

        private String namespace;

        private String description;

        private String pullCount;

        private String logo;

        private String publisher;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPullCount() {
            return pullCount;
        }

        public void setPullCount(String pullCount) {
            this.pullCount = pullCount;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }
    }
}
