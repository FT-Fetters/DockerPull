package com.heybcat.docker.pull.web.entity.view;

import java.util.List;

/**
 * @author Fetters
 */
public class LocalImagesView {

    /**
     * total file count
     */
    private Integer total;
    /**
     * current page
     */
    private Integer cur;

    private List<LocalImage> images;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCur() {
        return cur;
    }

    public void setCur(Integer cur) {
        this.cur = cur;
    }

    public List<LocalImage> getImages() {
        return images;
    }

    public void setImages(
        List<LocalImage> images) {
        this.images = images;
    }

    public static class LocalImage {
        private String fileName;

        private String createdTime;

        private Long size;


        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }
    }

}
