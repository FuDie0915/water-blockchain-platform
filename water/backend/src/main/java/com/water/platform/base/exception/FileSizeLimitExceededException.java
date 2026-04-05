package com.water.platform.base.exception;


public class FileSizeLimitExceededException extends RuntimeException  {

    /**
     * 文件大小
     */

    private final long size;

    public FileSizeLimitExceededException(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

}
